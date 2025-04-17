package com.example.demo.dto;

import java.util.List;

public record QuizQuestionDTO(
        Long id,
        String questionText,
        List<String> options
        // --- Relationship ---
        // Long quizId; // Often unnecessary if QuizQuestionDTO is always nested within QuizDTO
) {}