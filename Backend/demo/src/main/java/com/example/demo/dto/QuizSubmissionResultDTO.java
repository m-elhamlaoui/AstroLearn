package com.example.demo.dto;

public record QuizSubmissionResultDTO(
    Long completionId,
    int rawScore,
    int totalQuestions,
    boolean isPerfected,
    int experienceEarned // Good to include this here as well
) {}
