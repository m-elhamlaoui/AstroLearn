package com.example.demo.dto;

// DTO specifically for creating/updating answers (sent from client)

public record QuizQuestionAnswerDTO(
        Long questionId,
        int chosenOptionIndex
) {}