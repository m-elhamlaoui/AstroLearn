package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ArticleDTO(
        Long id,
        String title,
        String summary,
        String content,
        String imageUrl,
        LocalDateTime createdAt,

        // --- Relationships ---
        Long authorId,           // ID of the User who wrote it
        String authorUsername,   // Convenient display field

        Double averageRating,
        Long commentCount,

        Set<String> tags
) {
}
