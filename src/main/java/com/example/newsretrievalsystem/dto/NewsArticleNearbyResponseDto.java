package com.example.newsretrievalsystem.dto;

import com.example.newsretrievalsystem.model.NewsArticle;
import com.example.newsretrievalsystem.repository.NewsArticleRepository;
import com.example.newsretrievalsystem.util.Haversine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsArticleNearbyResponseDto extends NewsArticleResponseDto {
    private double distanceKm;
}