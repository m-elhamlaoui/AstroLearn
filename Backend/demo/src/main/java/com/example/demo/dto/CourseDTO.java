package com.example.demo.dto;

import com.example.demo.model.*;

import java.util.List;

public record CourseDTO(
        Long id,
        String title,
        String description,
        Course.DifficultyLevel difficulty,
        int totalLessons
) {}

