package com.example.demo.dto;

public record LessonDTO(
        Long id,
        String title,
        String content,
        String videoUrl,
        QuizDTO quiz,
        Long moduleId
) {
}
