package com.example.demo.dto;

import java.util.List;

public record QuizDTO(
        Long id,
        String title,
        List<QuizQuestionDTO> questions,
        int experienceReward
) {
}
