package com.example.demo.integration;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.model.User;
import com.example.demo.model.VoteType;
import com.example.demo.service.ArticleService;
import com.example.demo.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RecommendationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ArticleService articleService;

    private Long articleId1;
    private Long articleId2;
    private Long articleId3;
    private Long testUserId2;
    private Long testUserId3;

    @BeforeEach
    void setUp() {
        setUpTestUser();
        setUpAdditionalTestUsers();
        createTestArticles();
    }

    private void setUpAdditionalTestUsers() {
        // Create second test user
        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        user2.setRole(User.UserRole.USER);
        user2.setVerificationStatus(User.UserVerification.VERIFIED);
        testUserId2 = userRepository.save(user2).getId();

        // Create third test user
        User user3 = new User();
        user3.setUsername("testuser3");
        user3.setEmail("test3@example.com");
        user3.setPassword("password");
        user3.setRole(User.UserRole.USER);
        user3.setVerificationStatus(User.UserVerification.VERIFIED);
        testUserId3 = userRepository.save(user3).getId();
    }

    private void createTestArticles() {
        // Create test articles with different scores to test recommendations
        LocalDateTime now = LocalDateTime.now();

        // Article 1 - High score
        Set<String> tags1 = new HashSet<>();
        tags1.add("astronomy");
        ArticleDTO article1 = new ArticleDTO(
                null,
                "Popular Astronomy Article",
                "Summary 1",
                "Content 1",
                "image1.jpg",
                now,
                testUserId,
                "testuser",
                0,
                0L,
                tags1);
        ArticleDTO savedArticle1 = articleService.createArticle(article1, testUserId);
        articleId1 = savedArticle1.id();

        // Article 2 - Medium score
        Set<String> tags2 = new HashSet<>();
        tags2.add("space");
        ArticleDTO article2 = new ArticleDTO(
                null,
                "Space Exploration",
                "Summary 2",
                "Content 2",
                "image2.jpg",
                now.plusDays(1),
                testUserId,
                "testuser",
                0,
                0L,
                tags2);
        ArticleDTO savedArticle2 = articleService.createArticle(article2, testUserId);
        articleId2 = savedArticle2.id();

        // Article 3 - Low score
        Set<String> tags3 = new HashSet<>();
        tags3.add("physics");
        ArticleDTO article3 = new ArticleDTO(
                null,
                "Physics Basics",
                "Summary 3",
                "Content 3",
                "image3.jpg",
                now.plusDays(2),
                testUserId,
                "testuser",
                0,
                0L,
                tags3);
        ArticleDTO savedArticle3 = articleService.createArticle(article3, testUserId);
        articleId3 = savedArticle3.id();

        // Add votes from different users to create different popularity levels
        articleService.voteArticle(articleId1, testUserId, new ArticleVoteRequestDTO(VoteType.UP));
        articleService.voteArticle(articleId1, testUserId2, new ArticleVoteRequestDTO(VoteType.UP));
        articleService.voteArticle(articleId2, testUserId3, new ArticleVoteRequestDTO(VoteType.UP));
    }

    @Test
    void shouldGetArticleRecommendationsForUser() {
        // When
        List<ArticleDTO> recommendations = recommendationService.getArticleRecommendationsForUser(testUserId, 2);

        // Then
        assertThat(recommendations).isNotNull();
        assertThat(recommendations.size()).isLessThanOrEqualTo(2);

        // Verify that recommendations are returned in order of popularity
        if (recommendations.size() >= 2) {
            ArticleDTO firstRecommendation = recommendations.get(0);
            ArticleDTO secondRecommendation = recommendations.get(1);

            // The first article should have a higher score than the second
            assertThat(firstRecommendation.score()).isGreaterThanOrEqualTo(secondRecommendation.score());
        }
    }

    @Test
    void shouldGetPopularArticles() {
        // When
        List<ArticleDTO> popularArticles = recommendationService.getPopularArticles(2);

        // Then
        assertThat(popularArticles).isNotNull();
        assertThat(popularArticles.size()).isLessThanOrEqualTo(2);

        // Verify that articles are returned in order of popularity
        if (popularArticles.size() >= 2) {
            ArticleDTO firstArticle = popularArticles.get(0);
            ArticleDTO secondArticle = popularArticles.get(1);

            // The first article should have a higher score than the second
            assertThat(firstArticle.score()).isGreaterThanOrEqualTo(secondArticle.score());
        }
    }
}