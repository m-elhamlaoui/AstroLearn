package com.example.demo.dto;

public record ReadingHistoryDTO(
        Long articleId,
        String articleTitle,
        int timeSpentSeconds,
        boolean isRead
) {
}
