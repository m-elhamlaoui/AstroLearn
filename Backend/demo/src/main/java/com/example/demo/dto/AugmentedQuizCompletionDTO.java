package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AugmentedQuizCompletionDTO(
    Long id,                 // Unique ID for this specific quiz completion/attempt
    int rawScore,            // The number of questions the user answered correctly
    int totalQuestions,      // The total number of questions in this quiz
    LocalDateTime completionDate, // Timestamp of when the quiz was completed
    Long userId,
    String username,         // Username of the user who took the quiz
    Long quizId,
    String quizTitle,
    int experienceEarned,    // Experience points awarded for this attempt
    List<QuizAttemptDetailDTO> attemptDetails // List of details for each question
) {}
