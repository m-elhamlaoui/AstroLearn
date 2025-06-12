package com.example.demo.dto;

import java.time.LocalDateTime;

public record ReadingHistoryDTO(
        Long id, // Added ID as per UML convention
        boolean isRead,
        int timeSpentSeconds,
        LocalDateTime lastAccessed,
        // --- Relationships ---
        Long userId,
        Long articleId,
        String articleTitle // Convenient display field
) {}