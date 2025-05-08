package com.example.demo.dto;

import java.time.LocalDateTime;

public record RecommendationDTO(
        Long id,
        Long userId,
        Long articleId,
        double score,
        LocalDateTime createdAt) {
}