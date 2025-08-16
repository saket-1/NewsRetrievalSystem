package com.example.newsretrievalsystem.repository;

import com.example.newsretrievalsystem.model.NewsArticle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, String> {

    // For the "category" endpoint
    List<NewsArticle> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // For the "search" endpoint
    List<NewsArticle> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    // --- NEW METHODS FOR STEP 4 ---

    // For the "source" endpoint
    List<NewsArticle> findBySourceNameContainingIgnoreCase(String sourceName, Pageable pageable);

    // For the "score" endpoint
    List<NewsArticle> findByRelevanceScoreGreaterThan(double score, Pageable pageable);
}