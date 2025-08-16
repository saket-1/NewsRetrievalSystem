package com.example.newsretrievalsystem.repository;

import com.example.newsretrievalsystem.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    // Find all interactions that happened after a certain time
    List<UserInteraction> findByTimestampAfter(LocalDateTime timestamp);
}