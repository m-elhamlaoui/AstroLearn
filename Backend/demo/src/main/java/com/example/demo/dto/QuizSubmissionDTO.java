package com.example.demo.dto;

import java.util.List;

public record QuizSubmissionDTO(
        Long userId, // Or get from security context
        List<QuizQuestionAnswerDTO> answers
) {}
