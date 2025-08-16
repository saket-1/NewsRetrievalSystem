package com.example.newsretrievalsystem.controller;

import com.example.newsretrievalsystem.dto.LlmResponseDto;
import com.example.newsretrievalsystem.dto.NewsArticleNearbyResponseDto;
import com.example.newsretrievalsystem.dto.NewsArticleResponseDto;
import com.example.newsretrievalsystem.dto.UserQueryDto;
import com.example.newsretrievalsystem.model.NewsArticle;
import com.example.newsretrievalsystem.service.LlmService;
import com.example.newsretrievalsystem.service.NewsService;
import com.example.newsretrievalsystem.util.Haversine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired // Inject the new LLM service
    private LlmService llmService;

    @GetMapping("/category")
    public ResponseEntity<?> getNewsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(Collections.singletonMap("articles", newsService.getNewsByCategory(category)));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNews(@RequestParam String query) {
        return ResponseEntity.ok(Collections.singletonMap("articles", newsService.searchNews(query)));
    }

    // --- NEW ENDPOINTS FOR STEP 4 ---

    @GetMapping("/source")
    public ResponseEntity<?> getNewsBySource(@RequestParam String source) {
       return ResponseEntity.ok(Collections.singletonMap("articles", newsService.getNewsBySource(source)));
    }

    @GetMapping("/score")
    public ResponseEntity<?> getNewsByScore(@RequestParam double threshold) {
        return ResponseEntity.ok(Collections.singletonMap("articles", newsService.getNewsByScore(threshold)));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyNews(@RequestParam double lat, @RequestParam double lon, @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(Collections.singletonMap("articles", newsService.getNearbyNews(lat, lon, radius)));
    }

    @PostMapping("/query")
    public ResponseEntity<?> getNewsFromQuery(@RequestBody UserQueryDto userQueryDto) {
        try {
            LlmResponseDto analysis = llmService.analyzeQuery(userQueryDto.getQuery());
            String intent = analysis.getIntent();
            LlmResponseDto.Entities entities = analysis.getEntities();

            List<NewsArticleResponseDto> responseDtos;

            switch (intent.toLowerCase()) {
                case "category":
                    responseDtos = newsService.getNewsByCategory(entities.getCategory());
                    break;
                case "source":
                    responseDtos = newsService.getNewsBySource(entities.getSourceName());
                    break;
                case "search":
                    responseDtos = newsService.searchNews(entities.getSearchQuery());
                    break;
                case "nearby":
                    if (userQueryDto.getLatitude() == null || userQueryDto.getLongitude() == null) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Location (latitude, longitude) is required for a 'nearby' query."));
                    }
                    responseDtos = newsService.getNearbyNews(userQueryDto.getLatitude(), userQueryDto.getLongitude(), 10.0);
                    break;
                default:
                    responseDtos = newsService.searchNews(userQueryDto.getQuery());
                    break;
            }
            return ResponseEntity.ok(Collections.singletonMap("articles", responseDtos));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Failed to process query with LLM."));
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingNews(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") int limit) {

        // Geospatial clustering for more effective caching
        // Round lat/lon to 2 decimal places to create cache "zones"
        double cachedLat = Math.round(lat * 100.0) / 100.0;
        double cachedLon = Math.round(lon * 100.0) / 100.0;

        List<NewsArticleResponseDto> trendingArticles = newsService.getTrendingNews(cachedLat, cachedLon, limit);
        return ResponseEntity.ok(Collections.singletonMap("articles", trendingArticles));
    }

}