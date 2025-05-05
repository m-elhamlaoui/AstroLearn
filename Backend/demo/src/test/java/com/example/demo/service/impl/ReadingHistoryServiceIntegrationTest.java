package com.example.demo.integration;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.service.ArticleService;
import com.example.demo.service.ReadingHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingHistoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReadingHistoryService readingHistoryService;

    @Autowired
    private ArticleService articleService;

    private Long articleId;

    @BeforeEach
    void setUp() {
        setUpTestUser();
        createTestArticle();
    }

    private void createTestArticle() {
        // Create a test article
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = new HashSet<>();
        tags.add("astronomy");

        ArticleDTO articleDTO = new ArticleDTO(
            null,
            "Test Article for Reading History",
            "Summary",
            "Content",
            "image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags
        );

        ArticleDTO savedArticle = articleService.createArticle(articleDTO, testUserId);
        articleId = savedArticle.id();
    }

    @Test
    void shouldLogReadingTime() {
        // When
        ReadingHistoryDTO readingHistory = readingHistoryService.logReadingTime(testUserId, articleId, 60);

        // Then
        assertThat(readingHistory).isNotNull();
        assertThat(readingHistory.userId()).isEqualTo(testUserId);
        assertThat(readingHistory.articleId()).isEqualTo(articleId);
        assertThat(readingHistory.timeSpentSeconds()).isEqualTo(60);
        assertThat(readingHistory.isRead()).isFalse(); // Not enough time to mark as read
    }

    @Test
    void shouldMarkArticleAsReadAfterSufficientTime() {
        // When logging enough time to mark as read (assuming threshold is 300 seconds)
        ReadingHistoryDTO readingHistory = readingHistoryService.logReadingTime(testUserId, articleId, 300);

        // Then
        assertThat(readingHistory.isRead()).isTrue();
    }

    @Test
    void shouldAccumulateReadingTime() {
        // When logging time in multiple sessions
        ReadingHistoryDTO firstSession = readingHistoryService.logReadingTime(testUserId, articleId, 60);
        ReadingHistoryDTO secondSession = readingHistoryService.logReadingTime(testUserId, articleId, 90);

        // Then
        assertThat(secondSession.timeSpentSeconds()).isEqualTo(150);
    }

    @Test
    void shouldGetRecentlyReadArticles() {
        // Given
        readingHistoryService.logReadingTime(testUserId, articleId, 60);

        // When
        List<ReadingHistoryDTO> recentlyRead = readingHistoryService.getRecentlyReadArticles(testUserId);

        // Then
        assertThat(recentlyRead).isNotNull();
        assertThat(recentlyRead).hasSize(1);
        assertThat(recentlyRead.get(0).articleId()).isEqualTo(articleId);
    }

    @Test
    void shouldGetReadArticleIds() {
        // Given
        readingHistoryService.logReadingTime(testUserId, articleId, 300); // Enough time to mark as read

        // When
        List<Long> readArticleIds = readingHistoryService.getReadArticleIds(testUserId);

        // Then
        assertThat(readArticleIds).isNotNull();
        assertThat(readArticleIds).contains(articleId);
    }
} 