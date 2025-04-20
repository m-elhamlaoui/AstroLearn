package com.example.demo.service.impl;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Article;
import com.example.demo.model.ArticleTag; // Import ArticleTag
import com.example.demo.repository.ArticleRatingRepository;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ReadingHistoryRepository;
import com.example.demo.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Add logging
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j // Simple Logging Facade for Java
public class RecommendationServiceImpl implements RecommendationService {

    private final ArticleRepository articleRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ArticleRatingRepository articleRatingRepository;
    private final EntityMapper entityMapper;

    private static final int MIN_RATING_FOR_PREFERENCE = 4; // Configurable threshold for liked articles

    /**
     * Provides personalized article recommendations based on user's reading history and ratings.
     * Finds tags the user likes and recommends other articles with those tags,
     * excluding articles already read/rated highly or written by the user.
     * Falls back to generally popular articles if insufficient preference data exists.
     *
     * @param userId The ID of the user to get recommendations for.
     * @param count  The maximum number of recommendations to return.
     * @return A list of recommended ArticleDTOs.
     */
    @Override
    public List<ArticleDTO> getArticleRecommendationsForUser(Long userId, int count) {
        log.debug("Generating recommendations for User ID: {} (count: {})", userId, count);

        // --- Step 1: Identify articles the user has interacted positively with ---
        Set<Long> readArticleIds = readingHistoryRepository.findReadArticleIdsByUserId(userId);
        Set<Long> highlyRatedArticleIds = articleRatingRepository.findHighlyRatedArticleIdsByUserId(userId, MIN_RATING_FOR_PREFERENCE);

        Set<Long> positiveInteractionArticleIds = new HashSet<>(readArticleIds);
        positiveInteractionArticleIds.addAll(highlyRatedArticleIds);

        if (positiveInteractionArticleIds.isEmpty()) {
            log.debug("No positive interaction data found for user {}. Falling back to popular articles.", userId);
            return getPopularArticles(count); // Fallback if no history/ratings
        }

        // --- Step 2: Extract preferred tags from these articles ---
        List<Article> interactedArticles = articleRepository.findByIdInWithTagsAndAuthor(positiveInteractionArticleIds); // Fetch articles with tags
        Set<String> preferredTags = interactedArticles.stream()
                .flatMap(article -> article.getTags().stream()) // Get all tags from all articles
                .map(ArticleTag::getName) // Extract tag names
                .collect(Collectors.toSet());

        if (preferredTags.isEmpty()) {
            log.debug("No preferred tags found for user {} based on interactions. Falling back to popular articles.", userId);
            return getPopularArticles(count); // Fallback if interacted articles had no tags
        }
        log.debug("User {} preferred tags: {}", userId, preferredTags);

        // --- Step 3: Find candidate articles matching preferred tags ---
        // We need to exclude articles the user has already interacted with positively
        // AND articles the user wrote themselves.
        Pageable pageable = PageRequest.of(0, count * 2); // Fetch more initially in case filtering removes many

        List<Article> candidateArticles = articleRepository.findArticlesByTagNamesExcludingIdsAndAuthor(
                preferredTags,
                positiveInteractionArticleIds, // Exclude positively interacted articles
                userId, // Exclude user's own articles
                (java.awt.print.Pageable) pageable
        );
        log.debug("Found {} initial candidate articles for user {} based on tags.", candidateArticles.size(), userId);


        // --- Step 4: Limit and Map to DTO ---
        // The query already sorted by rating/date, so just take the top 'count'
        List<ArticleDTO> recommendations = candidateArticles.stream()
                .limit(count)
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Generated {} recommendations for User ID: {}", recommendations.size(), userId);
        return recommendations;
    }

    /**
     * Simple Popularity: Returns the top 'count' articles sorted by averageRating descending,
     * excluding articles written by the target user (if recommendations fall back to this).
     *
     * @param count The maximum number of articles to return.
     * @return A list of popular (highest-rated) ArticleDTOs.
     */
    @Override
    public List<ArticleDTO> getPopularArticles(int count) {
        // This method is now primarily a fallback for personalized recommendations.
        // It might be called directly elsewhere too.
        // For the fallback case, we don't have a specific user ID to exclude their articles,
        // but ideally, the personalized method handles that exclusion.
        // If called directly, consider if exclusion is needed. Let's assume not for direct call.

        log.debug("--- Simple Recommendation --- Fetching top {} articles by average rating.", count);
        Pageable topRatedPage = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "averageRating", "createdAt")); // Add secondary sort

        List<Article> popularArticles = articleRepository.findAll(topRatedPage).getContent();

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Overloaded popular articles method used as fallback from personalized recommendations.
     * Excludes articles written by the specified user.
     *
     * @param count The maximum number of articles to return.
     * @param userIdToExclude The user ID whose articles should be excluded.
     * @return A list of popular (highest-rated) ArticleDTOs excluding the user's own.
     */
    // Note: Kept the original getPopularArticles as public, added this specific one for fallback
    private List<ArticleDTO> getPopularArticles(int count, Long userIdToExclude) {
        log.debug("--- Simple Recommendation Fallback --- Fetching top {} articles by average rating, excluding user {}.", count, userIdToExclude);
        Pageable topRatedPage = PageRequest.of(0, count); // Sorting is done in the query

        List<Article> popularArticles = articleRepository.findTopRatedArticlesExcludingAuthor(userIdToExclude, (java.awt.print.Pageable) topRatedPage);

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }


}