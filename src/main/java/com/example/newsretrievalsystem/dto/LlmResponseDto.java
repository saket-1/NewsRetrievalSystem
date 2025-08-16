package com.example.newsretrievalsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LlmResponseDto {
    private String intent;
    private Entities entities;

    @Data
    public static class Entities {
        @JsonProperty("source_name")
        private String sourceName;
        private String category;
        @JsonProperty("search_query")
        private String searchQuery;
        private String location;
    }
}