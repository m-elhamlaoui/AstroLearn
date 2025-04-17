package com.example.demo.dto;

import java.util.List;

public record ModuleDTO(
        Long id,
        String title,
        Long courseId,
        int lessonCount,
        List<Long> lessonIds
) {
}
