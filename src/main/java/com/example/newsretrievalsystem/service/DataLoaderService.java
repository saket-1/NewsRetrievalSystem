package com.example.newsretrievalsystem.service;

import com.example.newsretrievalsystem.model.NewsArticle;
import com.example.newsretrievalsystem.repository.NewsArticleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.InputStream;
import java.util.List;

@Service
public class DataLoaderService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void loadData() {
        // Only load data if the database is empty
        if (newsArticleRepository.count() == 0) {
            try {
                Resource resource = resourceLoader.getResource("classpath:news_data.json");
                InputStream inputStream = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule()); // To handle LocalDateTime

                List<NewsArticle> articles = mapper.readValue(inputStream, new TypeReference<List<NewsArticle>>() {});
                newsArticleRepository.saveAll(articles);
                System.out.println(articles.size() + " news articles have been loaded into the database.");
            } catch (Exception e) {
                throw new RuntimeException("Failed to load news data from JSON file", e);
            }
        }
    }
}