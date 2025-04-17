package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record CourseProgressDTO(
        Long id,
        double completionPercentage,
        boolean completed,
        LocalDateTime lastAccessed,
        // --- Relationships ---
        Long userId,
        Long courseId,
        Long currentLessonId, // ID of the Lesson the user is currently on (optional)
        Set<Long> completedLessonIds // If needed for detailed tracking
) {}