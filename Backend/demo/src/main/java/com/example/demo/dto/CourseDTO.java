package com.example.demo.dto;

import com.example.demo.model.Course;

import java.util.List;

public record CourseDTO(
        Long id,
        String title,
        String description,
        Course.DifficultyLevel difficulty,
        List<ModuleDTO> modules
) {}

