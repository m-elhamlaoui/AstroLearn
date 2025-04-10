package com.example.demo.dto;

import java.util.List;

public record ModuleDTO(
        Long id,
        String title,
        List<LessonDTO> lessons
) {
}
