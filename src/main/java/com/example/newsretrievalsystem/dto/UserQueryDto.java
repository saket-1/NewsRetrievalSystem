package com.example.newsretrievalsystem.dto;

import lombok.Data;

@Data
public class UserQueryDto {
    private String query;
    private Double latitude;
    private Double longitude;
}