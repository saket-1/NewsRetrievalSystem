package com.example.newsretrievalsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String articleId;
    private String userId; // To simulate different users

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private double latitude;
    private double longitude;

    private LocalDateTime timestamp;

    public enum EventType {
        VIEW, CLICK, SHARE
    }
}