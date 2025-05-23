package com.example.demo.dto;

import com.example.demo.model.*;

import java.util.List;

public record CourseDTO(
        Long id,
        String title,
        String description,
        String imageUrl,
        Course.DifficultyLevel difficulty,
        int totalLessons,
        List<Long> moduleIds,
        Course.CourseStatus status
) {}

