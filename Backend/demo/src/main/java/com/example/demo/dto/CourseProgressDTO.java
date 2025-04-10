package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record CourseProgressDTO(
        Long id,
        Long userId,
        Long courseId,
        Long currentLessonId,
        double completionPercentage,
        boolean completed,
        Set<Long> completedLessonIds,
        LocalDateTime lastAccessed
) {}