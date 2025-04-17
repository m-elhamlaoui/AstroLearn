package com.example.demo.dto;

import java.time.LocalDateTime;

public record QuizCompletionDTO(
        Long id,
        int score,
        LocalDateTime completionDate,
        Long userId,
        String username, // Convenient display field
        Long quizId,
        String quizTitle // Convenient display field
) {}