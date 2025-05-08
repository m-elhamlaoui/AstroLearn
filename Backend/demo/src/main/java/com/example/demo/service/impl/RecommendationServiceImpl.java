package com.example.demo.service.impl;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Article;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ArticleVoteRepository;
import com.example.demo.repository.ReadingHistoryRepository;
import com.example.demo.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ArticleRepository articleRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ArticleVoteRepository articleVoteRepository;
    private final EntityMapper entityMapper;

    @Override
    public List<ArticleDTO> getArticleRecommendationsForUser(Long userId, int count) {
        log.debug("Generating recommendations for User ID: {} (count: {})", userId, count);

        // --- Step 1: Identify articles the user has interacted positively with (Read or Upvoted) ---
        Set<Long> readArticleIds = readingHistoryRepository.findReadArticleIdsByUserId(userId);
        Set<Long> upvotedArticleIds = articleVoteRepository.findUpvotedArticleIdsByUserId(userId);

        Set<Long> positiveInteractionArticleIds = new HashSet<>(readArticleIds);
        positiveInteractionArticleIds.addAll(upvotedArticleIds);

        if (positiveInteractionArticleIds.isEmpty()) {
            log.debug("No positive interaction (read/upvoted) data found for user {}. Falling back to popular articles.", userId);
            return getPopularArticlesFallback(count, userId);
        }

        log.debug("User {} positive interaction article IDs: {}", userId, positiveInteractionArticleIds);

        // --- Step 2: Extract preferred tags from these articles ---
        List<Article> interactedArticles = articleRepository.findAllById(positiveInteractionArticleIds);

        Set<String> preferredTags = interactedArticles.stream()
                .filter(article -> article.getTags() != null)
                .flatMap(article -> article.getTags().stream())
                .map(tag -> tag.getTagName().getName())
                .collect(Collectors.toSet());

        if (preferredTags.isEmpty()) {
            log.debug("No preferred tags found for user {} based on interactions. Falling back to popular articles.", userId);
            return getPopularArticlesFallback(count, userId);
        }

        log.debug("User {} preferred tags: {}", userId, preferredTags);

        // --- Step 3: Find candidate articles matching preferred tags ---
        Pageable pageable = PageRequest.of(0, count * 2); // Fetch more articles to allow filtering
        List<Article> candidateArticles = articleRepository.findByTagsAndExcludeIdsAndAuthorOrderByScore(
                preferredTags,
                positiveInteractionArticleIds,
                userId,
                pageable
        );

        log.debug("Found {} candidate articles for user {} based on tags.", candidateArticles.size(), userId);

        // --- Step 4: Limit and Map to DTO ---
        List<ArticleDTO> recommendations = candidateArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        // If fewer than 'count' candidates found, supplement with popular articles
        if (recommendations.size() < count) {
            log.debug("Found only {} tag-based recommendations for user {}. Supplementing with popular articles.", recommendations.size(), userId);
            int needed = count - recommendations.size();
            Set<Long> existingRecommendationIds = recommendations.stream()
                    .map(ArticleDTO::id)
                    .collect(Collectors.toSet());
            existingRecommendationIds.addAll(positiveInteractionArticleIds);

            List<ArticleDTO> popularSupplement = getPopularArticlesFallback(needed, userId, existingRecommendationIds);
            recommendations.addAll(popularSupplement);
        }

        log.info("Generated {} recommendations for User ID: {}", recommendations.size(), userId);
        return recommendations;
    }

    private List<ArticleDTO> getPopularArticlesFallback(int count, Long userIdToExclude) {
        log.debug("Fetching top {} articles by score, excluding user {}.", count, userIdToExclude);
        Pageable topScoringPage = PageRequest.of(0, count);

        List<Article> popularArticles = articleRepository.findTopScoringArticlesExcludingAuthor(userIdToExclude, topScoringPage);

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    private List<ArticleDTO> getPopularArticlesFallback(int count, Long userIdToExclude, Set<Long> articleIdsToExclude) {
        log.debug("Supplementing recommendations with popular articles, excluding user {} and {} specific article IDs.", userIdToExclude, articleIdsToExclude.size());

        Pageable topScoringPage = PageRequest.of(0, count);

        List<Article> popularArticles = articleRepository.findTopScoringArticlesExcludingAuthorAndIds(
                userIdToExclude,
                articleIdsToExclude.isEmpty() ? Set.of(-1L) : articleIdsToExclude, // Handle empty set for query
                topScoringPage
        );

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleDTO> getPopularArticles(int count) {
        log.debug("Fetching top {} popular articles by score.", count);
        Pageable topScoringPage = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "score", "createdAt"));

        List<Article> popularArticles = articleRepository.findAll(topScoringPage).getContent();

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}
