package com.example.demo.dto;

public record CommentDTO(
        Long id,
        String content,
        Long userId,
        Long articleId,
        String createdAt
) {
}
