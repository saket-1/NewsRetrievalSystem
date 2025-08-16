package com.example.newsretrievalsystem.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class NewsArticle {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String url;
    @JsonProperty("publication_date")
    private LocalDateTime publicationDate;
    @JsonProperty("source_name")
    private String sourceName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> category;

    @JsonProperty("relevance_score")
    private double relevanceScore;
    private double latitude;
    private double longitude;
}