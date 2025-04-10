package com.example.demo.dto;

import java.util.List;

public record QuizQuestionDTO(
        Long id,
        Long quizId,
        String questionText,
        List<String> options,
        int correctOptionIndex
) {}