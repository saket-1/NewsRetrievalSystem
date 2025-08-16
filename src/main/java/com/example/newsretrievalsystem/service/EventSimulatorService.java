package com.example.newsretrievalsystem.service;

import com.example.newsretrievalsystem.model.NewsArticle;
import com.example.newsretrievalsystem.model.UserInteraction;
import com.example.newsretrievalsystem.repository.NewsArticleRepository;
import com.example.newsretrievalsystem.repository.UserInteractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EventSimulatorService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    private final Random random = new Random();

    @Async
    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void simulateUserActivity() {
        List<NewsArticle> articles = newsArticleRepository.findAll();
        if (articles.isEmpty()) {
            return;
        }

        // Simulate a random number of events (e.g., 1 to 5) in this cycle
        int numberOfEvents = random.nextInt(5) + 1;
        for (int i = 0; i < numberOfEvents; i++) {
            NewsArticle randomArticle = articles.get(random.nextInt(articles.size()));
            UserInteraction interaction = new UserInteraction();

            interaction.setArticleId(randomArticle.getId());
            interaction.setUserId("user-" + random.nextInt(100)); // Simulate 100 unique users
            interaction.setEventType(UserInteraction.EventType.values()[random.nextInt(UserInteraction.EventType.values().length)]);

            // Simulate the event happening near the article's location
            interaction.setLatitude(randomArticle.getLatitude() + (random.nextDouble() - 0.5));
            interaction.setLongitude(randomArticle.getLongitude() + (random.nextDouble() - 0.5));
            interaction.setTimestamp(LocalDateTime.now());

            userInteractionRepository.save(interaction);
        }
        System.out.println("Simulated " + numberOfEvents + " new user interactions.");
    }
}