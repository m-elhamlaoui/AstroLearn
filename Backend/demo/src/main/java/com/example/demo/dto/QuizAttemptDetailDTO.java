package com.example.demo.dto;

import java.util.List;

public record QuizAttemptDetailDTO(
    Long questionId,
    String questionText,     // The full text of the question
    List<String> options,    // The list of options presented to the user
    int chosenOptionIndex,   // The 0-based index of the option the user selected
    int correctOptionIndex,  // The 0-based index of the correct option
    boolean isCorrect        // Convenience field: true if chosenOptionIndex == correctOptionIndex
) {}
