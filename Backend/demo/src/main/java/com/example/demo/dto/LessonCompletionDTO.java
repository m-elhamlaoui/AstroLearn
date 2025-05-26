package com.example.demo.dto;

import java.time.LocalDateTime;

public record LessonCompletionDTO(
    Long id,
    Long userId,
    String username,
    Long lessonId,
    String lessonTitle,
    Long courseId,
    String courseTitle,
    LocalDateTime completionDate
) {}
