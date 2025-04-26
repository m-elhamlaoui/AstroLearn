package com.example.demo.service.impl;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Article;
import com.example.demo.model.ArticleTag;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ArticleVoteRepository; // CHANGED: Use Vote Repository
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
    private final ArticleVoteRepository articleVoteRepository; // CHANGED: Injected Vote Repository
    private final EntityMapper entityMapper;

    // REMOVED: MIN_RATING_FOR_PREFERENCE constant is no longer needed

    /**
     * Provides personalized article recommendations based on user's reading history and upvotes.
     * Finds tags the user likes (based on upvoted/read articles) and recommends other articles
     * with those tags, excluding articles already read/upvoted or written by the user.
     * Falls back to generally popular (high-score) articles if insufficient preference data exists.
     *
     * @param userId The ID of the user to get recommendations for.
     * @param count  The maximum number of recommendations to return.
     * @return A list of recommended ArticleDTOs.
     */
    @Override
    public List<ArticleDTO> getArticleRecommendationsForUser(Long userId, int count) {
        log.debug("Generating recommendations for User ID: {} (count: {})", userId, count);

        // --- Step 1: Identify articles the user has interacted positively with (Read or Upvoted) ---
        Set<Long> readArticleIds = readingHistoryRepository.findReadArticleIdsByUserId(userId);
        Set<Long> upvotedArticleIds = articleVoteRepository.findUpvotedArticleIdsByUserId(userId); // CHANGED: Fetch upvoted articles

        Set<Long> positiveInteractionArticleIds = new HashSet<>(readArticleIds);
        positiveInteractionArticleIds.addAll(upvotedArticleIds); // Combine read and upvoted

        if (positiveInteractionArticleIds.isEmpty()) {
            log.debug("No positive interaction (read/upvoted) data found for user {}. Falling back to popular articles.", userId);
            // Pass userId to exclude their own articles in the fallback
            return getPopularArticlesFallback(count, userId);
        }
        log.debug("User {} positive interaction article IDs: {}", userId, positiveInteractionArticleIds);


        // --- Step 2: Extract preferred tags from these articles ---
        // Ensure repository method fetches tags eagerly or handle potential LazyInitializationException
        List<Article> interactedArticles = articleRepository.findAllById(positiveInteractionArticleIds); // Fetch details of interacted articles
        // Consider a dedicated query `findByIdInAndFetchTags(ids)` for efficiency if needed

        Set<String> preferredTags = interactedArticles.stream()
                .filter(article -> article.getTags() != null) // Safety check
                .flatMap(article -> article.getTags().stream())
                .map(ArticleTag::getName)
                .collect(Collectors.toSet());

        if (preferredTags.isEmpty()) {
            log.debug("No preferred tags found for user {} based on interactions. Falling back to popular articles.", userId);
            // Pass userId to exclude their own articles in the fallback
            return getPopularArticlesFallback(count, userId);
        }
        log.debug("User {} preferred tags: {}", userId, preferredTags);

        // --- Step 3: Find candidate articles matching preferred tags ---
        // Exclude articles the user has positively interacted with AND articles the user wrote.
        // Fetch more initially, as filtering might remove some candidates. Max limit 'count'.
        // NOTE: Ensure the Repository method sorts by score DESC, createdAt DESC
        Pageable pageable = PageRequest.of(0, count * 2); // Fetch more to allow filtering; limit is applied later

        // ** IMPORTANT: Ensure this repository method exists and sorts correctly **
        List<Article> candidateArticles = articleRepository.findByTagsAndExcludeIdsAndAuthorOrderByScore(
                preferredTags,
                positiveInteractionArticleIds, // Exclude positively interacted articles
                userId, // Exclude user's own articles
                pageable // Apply pagination limit here
        );
        log.debug("Found {} initial candidate articles for user {} based on tags.", candidateArticles.size(), userId);


        // --- Step 4: Limit and Map to DTO ---
        // The query already sorted by score/date, so just take the top 'count'
        List<ArticleDTO> recommendations = candidateArticles.stream()
                // limit is now handled by Pageable in the query, but double-check if needed
                // .limit(count) // Usually redundant if Pageable limit is correct
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        // If fewer than 'count' candidates found, supplement with general popular articles (excluding overlaps)
        if (recommendations.size() < count) {
            log.debug("Found only {} tag-based recommendations for user {}. Supplementing with popular articles.", recommendations.size(), userId);
            int needed = count - recommendations.size();
            Set<Long> existingRecommendationIds = recommendations.stream().map(ArticleDTO::id).collect(Collectors.toSet());
            existingRecommendationIds.addAll(positiveInteractionArticleIds); // Also exclude already interacted articles

            List<ArticleDTO> popularSupplement = getPopularArticlesExcluding(needed, userId, existingRecommendationIds);
            recommendations.addAll(popularSupplement);
        }


        log.info("Generated {} recommendations for User ID: {}", recommendations.size(), userId);
        return recommendations;
    }

    /**
     * Gets popular articles, excluding those written by a specific user.
     * Primarily used as a fallback for personalized recommendations.
     *
     * @param count The maximum number of articles to return.
     * @param userIdToExclude The user ID whose articles should be excluded.
     * @return A list of popular (highest-score) ArticleDTOs excluding the user's own.
     */
    private List<ArticleDTO> getPopularArticlesFallback(int count, Long userIdToExclude) {
        log.debug("--- Simple Recommendation Fallback --- Fetching top {} articles by score, excluding user {}.", count, userIdToExclude);
        // Use the existing repo method which sorts by score
        Pageable topScoringPage = PageRequest.of(0, count); // Limit results

        List<Article> popularArticles = articleRepository.findTopScoringArticlesExcludingAuthor(userIdToExclude, topScoringPage);

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets popular articles, excluding those written by a specific user AND a set of already considered IDs.
     * Used to supplement personalized recommendations when not enough were found via tags.
     *
     * @param count The maximum number of articles to return.
     * @param userIdToExclude The user ID whose articles should be excluded.
     * @param articleIdsToExclude A set of article IDs to exclude (e.g., already recommended or interacted with).
     * @return A list of popular (highest-score) ArticleDTOs excluding specified user's and specified IDs.
     */
    private List<ArticleDTO> getPopularArticlesExcluding(int count, Long userIdToExclude, Set<Long> articleIdsToExclude) {
        log.debug("--- Supplementing Recommendations --- Fetching top {} articles by score, excluding user {} and {} specific article IDs.", count, userIdToExclude, articleIdsToExclude.size());

        Pageable topScoringPage = PageRequest.of(0, count); // Limit results

        // ** IMPORTANT: Ensure this repository method exists **
        List<Article> popularArticles = articleRepository.findTopScoringArticlesExcludingAuthorAndIds(
                userIdToExclude,
                articleIdsToExclude.isEmpty() ? Set.of(-1L) : articleIdsToExclude, // Handle empty set for query if needed
                 topScoringPage
        );

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Simple Popularity: Returns the top 'count' articles sorted by score descending.
     * This is a general method, doesn't exclude any specific user unless called via fallback.
     *
     * @param count The maximum number of articles to return.
     * @return A list of popular (highest-score) ArticleDTOs.
     */
    @Override
    public List<ArticleDTO> getPopularArticles(int count) {
        log.debug("--- General Popular Articles --- Fetching top {} articles by score.", count);
        // CHANGED: Sort by 'score' instead of 'averageRating'
        Pageable topScoringPage = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "score", "createdAt"));

        // Use standard findAll with sorting
        List<Article> popularArticles = articleRepository.findAll(topScoringPage).getContent();

        return popularArticles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}