package com.example.demo.dto;


public record ArticleRatingDTO(
        Long id,
        int rating,
        Long userId,
        Long articleId
) {}

