package com.example.demo.service.impl;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.repository.ArticleRepository; // Needed for popular articles fallback
import com.example.demo.mapper.EntityMapper;
import com.example.demo.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Recommendations are read-only
public class RecommendationServiceImpl implements RecommendationService {

    private final ArticleRepository articleRepository; // Example dependency
    private final EntityMapper entityMapper;

    @Override
    public List<ArticleDTO> getArticleRecommendationsForUser(Long userId, int count) {
        System.out.println("--- AI Placeholder --- Generating recommendations for User ID: " + userId);
        // TODO: Replace with actual AI/ML recommendation logic based on user history, profile, etc.
        // For now, return some recent or popular articles as a fallback.
        return getPopularArticles(count); // Fallback to popular
    }

    @Override
    public List<ArticleDTO> getPopularArticles(int count) {
        System.out.println("--- Placeholder --- Fetching popular articles (e.g., highest rated or most recent)");
        // TODO: Implement logic for popularity (e.g., sort by averageRating, commentCount, createdAt)
        // Example: Fetch most recent verified articles
        return articleRepository.findAll().stream() // Replace with a more specific query
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt())) // Sort recent first
                .limit(count)
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}