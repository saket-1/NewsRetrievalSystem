package com.example.newsretrievalsystem.service;

import com.example.newsretrievalsystem.dto.NewsArticleResponseDto;
import com.example.newsretrievalsystem.model.NewsArticle;
import com.example.newsretrievalsystem.model.UserInteraction;
import com.example.newsretrievalsystem.repository.NewsArticleRepository;
import com.example.newsretrievalsystem.repository.UserInteractionRepository;
import com.example.newsretrievalsystem.util.Haversine; // Import the utility
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired // Inject the LLM Service
    private LlmService llmService;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    public List<NewsArticleResponseDto> getNewsByCategory(String category) {
        List<NewsArticle> articles = newsArticleRepository.findByCategoryContainingIgnoreCase(
                category,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "publicationDate"))
        );
        return enrichArticlesWithSummaries(articles);
    }

    public List<NewsArticleResponseDto> searchNews(String query) {
        List<NewsArticle> articles = newsArticleRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "relevanceScore"))
        );
        return enrichArticlesWithSummaries(articles);
    }

    // --- NEW METHODS FOR STEP 4 ---

    public List<NewsArticleResponseDto> getNewsBySource(String source) {
        List<NewsArticle> articles = newsArticleRepository.findBySourceNameContainingIgnoreCase(
                source,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "publicationDate"))
        );
        return enrichArticlesWithSummaries(articles);
    }

    public List<NewsArticleResponseDto> getNewsByScore(double threshold) {
        List<NewsArticle> articles = newsArticleRepository.findByRelevanceScoreGreaterThan(
                threshold,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "relevanceScore"))
        );
        return enrichArticlesWithSummaries(articles);
    }

    public List<NewsArticleResponseDto> getNearbyNews(double lat, double lon, double radius) {
        List<NewsArticle> allArticles = newsArticleRepository.findAll();

        List<NewsArticle> nearbyArticles = allArticles.stream()
                .filter(article -> Haversine.distance(lat, lon, article.getLatitude(), article.getLongitude()) <= radius)
                .sorted(Comparator.comparingDouble(article -> Haversine.distance(lat, lon, article.getLatitude(), article.getLongitude())))
                .limit(5)
                .toList();
        return enrichArticlesWithSummaries(nearbyArticles);
    }

    @Cacheable(value = "trendingNews", key = "#lat.toString() + '_' + #lon.toString() + '_' + #limit")
    public List<NewsArticleResponseDto> getTrendingNews(double lat, double lon, int limit) {
        // 1. Fetch recent interactions (e.g., last 24 hours)
        List<UserInteraction> recentInteractions = userInteractionRepository.findByTimestampAfter(LocalDateTime.now().minusHours(24));

        // 2. Calculate a score for each interaction
        Map<String, Double> articleScores = recentInteractions.stream()
                .collect(Collectors.groupingBy(
                        UserInteraction::getArticleId,
                        Collectors.summingDouble(interaction -> calculateInteractionScore(interaction, lat, lon))
                ));

        // 3. Rank articles by score
        List<String> rankedArticleIds = articleScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        // 4. Fetch and enrich the top articles
        List<NewsArticle> trendingArticles = newsArticleRepository.findAllById(rankedArticleIds);
        return enrichArticlesWithSummaries(trendingArticles);
    }

    private double calculateInteractionScore(UserInteraction interaction, double userLat, double userLon) {
        double eventTypeWeight = switch (interaction.getEventType()) {
            case CLICK -> 1.5;
            case SHARE -> 2.0;
            default -> 1.0; // VIEW
        };

        // Recency: newer events get higher scores
        long hoursAgo = ChronoUnit.HOURS.between(interaction.getTimestamp(), LocalDateTime.now());
        double recencyWeight = 1.0 / (hoursAgo + 1);

        // Proximity: closer events get higher scores
        double distance = Haversine.distance(userLat, userLon, interaction.getLatitude(), interaction.getLongitude());
        double proximityWeight = 1.0 / (distance + 1); // +1 to avoid division by zero

        return eventTypeWeight * recencyWeight * proximityWeight;
    }

    private List<NewsArticleResponseDto> enrichArticlesWithSummaries(List<NewsArticle> articles) {
        // NOTE: These calls are sequential. For better performance in a production environment,
        // you could use parallel streams or asynchronous calls (CompletableFuture).
        return articles.stream().map(article -> {
            NewsArticleResponseDto dto = convertToDto(article);
            try {
                String summary = llmService.summarizeArticle(article.getTitle(), article.getDescription());
                dto.setLlmSummary(summary);
            } catch (IOException e) {
                dto.setLlmSummary("Summary not available.");
                // Log the error
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private NewsArticleResponseDto convertToDto(NewsArticle article) {
        NewsArticleResponseDto dto = new NewsArticleResponseDto();
        dto.setTitle(article.getTitle());
        dto.setDescription(article.getDescription());
        dto.setUrl(article.getUrl());
        dto.setPublicationDate(article.getPublicationDate().toString());
        dto.setSourceName(article.getSourceName());
        dto.setCategory(article.getCategory().toArray(new String[0]));
        dto.setRelevanceScore(article.getRelevanceScore());
        dto.setLatitude(article.getLatitude());
        dto.setLongitude(article.getLongitude());
        return dto;
    }
}