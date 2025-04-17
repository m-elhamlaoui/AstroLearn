package com.example.demo.dto;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        String content,
        Long articleId,
        LocalDateTime createdAt,
        // --- Relationships ---
        Long userId,
        String authorUsername // Convenient display field
) {
}