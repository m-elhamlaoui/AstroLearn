package com.example.demo.dto;

import java.time.LocalDateTime;

public record QuizCompletionDTO(
        Long id,
        Long userId,
        Long quizId,
        int score,
        LocalDateTime completionDate
) {}