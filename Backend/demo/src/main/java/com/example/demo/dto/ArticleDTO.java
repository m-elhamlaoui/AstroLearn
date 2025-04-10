package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ArticleDTO(
        Long id,
        String title,
        String summary,
        String content,
        String imageUrl,
        UserDTO author,
        LocalDateTime createdAt,
        Double averageRating,
        Set<String> tags,
        boolean verified
) {
}
