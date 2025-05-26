package com.example.demo.dto;

import java.util.List;

public record CourseProgressSummaryDTO(
    Long courseId,
    String courseTitle,
    Long userId,
    String username,
    int totalLessons,
    int completedLessons,
    double completionPercentage,
    boolean completed,
    Long currentLessonId,
    List<Long> completedLessonIds
) {}
