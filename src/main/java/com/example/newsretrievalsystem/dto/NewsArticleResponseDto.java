package com.example.newsretrievalsystem.dto;

import lombok.Data;

@Data
public class NewsArticleResponseDto {
    private String title;
    private String description;
    private String url;
    private String publicationDate;
    private String sourceName;
    private String[] category;
    private double relevanceScore;
    private String llmSummary;
    private double latitude;
    private double longitude;
}