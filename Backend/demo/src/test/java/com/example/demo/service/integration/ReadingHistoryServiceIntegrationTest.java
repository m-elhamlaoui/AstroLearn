package com.example.demo.service.integration;

import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.model.Article;
import com.example.demo.model.ReadingHistory;
import com.example.demo.model.User;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ReadingHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReadingHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ReadingHistoryServiceIntegrationTest {

    @Autowired
    private ReadingHistoryService readingHistoryService;

    @Autowired
    private ReadingHistoryRepository readingHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private User testUser;
    private Article testArticle1;
    private Article testArticle2;

    @BeforeEach
    public void setup() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // Create test articles
        testArticle1 = new Article();
        testArticle1.setTitle("Test Article 1");
        testArticle1.setContent("Test content 1");
        testArticle1.setAuthor(testUser);
        testArticle1.setCreatedAt(LocalDateTime.now());
        articleRepository.save(testArticle1);

        testArticle2 = new Article();
        testArticle2.setTitle("Test Article 2");
        testArticle2.setContent("Test content 2");
        testArticle2.setAuthor(testUser);
        testArticle2.setCreatedAt(LocalDateTime.now());
        articleRepository.save(testArticle2);
    }

    @Test
    public void testLogReadingTime_NewHistory() {
        // Test creating a new reading history entry
        ReadingHistoryDTO result = readingHistoryService.logReadingTime(testUser.getId(), testArticle1.getId(), 45);

        // Verify the result
        assertNotNull(result);
        assertEquals(testUser.getId(), result.userId());
        assertEquals(testArticle1.getId(), result.articleId());
        assertEquals(45, result.timeSpentSeconds());
        assertTrue(result.isRead()); // Should be true since timeSpentSeconds > 30

        // Verify it was saved in the repository
        Optional<ReadingHistory> savedHistory = readingHistoryRepository.findByUserAndArticle(testUser, testArticle1);
        assertTrue(savedHistory.isPresent());
        assertEquals(45, savedHistory.get().getTimeSpentSeconds());
        assertTrue(savedHistory.get().isRead());
    }

    @Test
    public void testGetRecentlyReadArticles() {
        // Create reading histories for both articles
        readingHistoryService.logReadingTime(testUser.getId(), testArticle1.getId(), 45);
        readingHistoryService.logReadingTime(testUser.getId(), testArticle2.getId(), 20);
        
        // Get recently read articles
        List<ReadingHistoryDTO> recentlyRead = readingHistoryService.getRecentlyReadArticles(testUser.getId());
        
        // Verify the result
        assertNotNull(recentlyRead);
        assertFalse(recentlyRead.isEmpty());
        
        // Check that both articles are in the list
        boolean foundArticle1 = false;
        boolean foundArticle2 = false;
        
        for (ReadingHistoryDTO dto : recentlyRead) {
            if (dto.articleId().equals(testArticle1.getId())) {
                foundArticle1 = true;
                assertEquals(testArticle1.getTitle(), dto.articleTitle());
            } else if (dto.articleId().equals(testArticle2.getId())) {
                foundArticle2 = true;
                assertEquals(testArticle2.getTitle(), dto.articleTitle());
            }
        }
        
        assertTrue(foundArticle1, "Article 1 should be in recently read list");
        assertTrue(foundArticle2, "Article 2 should be in recently read list");
    }

    @Test
    public void testGetReadArticleIds() {
        // Create reading histories with one article marked as read (> 30 seconds)
        readingHistoryService.logReadingTime(testUser.getId(), testArticle1.getId(), 45); // Will be marked as read
        readingHistoryService.logReadingTime(testUser.getId(), testArticle2.getId(), 20); // Will not be marked as read
        
        // Get read article IDs
        List<Long> readArticleIds = readingHistoryService.getReadArticleIds(testUser.getId());
        
        // Verify the result
        assertNotNull(readArticleIds);
        assertEquals(1, readArticleIds.size());
        assertTrue(readArticleIds.contains(testArticle1.getId()));
        assertFalse(readArticleIds.contains(testArticle2.getId()));
    }

    @Test
    public void testLogReadingTime_UserNotFound() {
        // Test with non-existent user ID
        Long nonExistentUserId = 999L;
        
        // Expect ResourceNotFoundException
        assertThrows(com.example.demo.exception.ResourceNotFoundException.class, () -> {
            readingHistoryService.logReadingTime(nonExistentUserId, testArticle1.getId(), 45);
        });
    }

    @Test
    public void testLogReadingTime_ArticleNotFound() {
        // Test with non-existent article ID
        Long nonExistentArticleId = 999L;
        
        // Expect ResourceNotFoundException
        assertThrows(com.example.demo.exception.ResourceNotFoundException.class, () -> {
            readingHistoryService.logReadingTime(testUser.getId(), nonExistentArticleId, 45);
        });
    }

    @Test
    public void testGetRecentlyReadArticles_UserNotFound() {
        // Test with non-existent user ID
        Long nonExistentUserId = 999L;
        
        // Expect ResourceNotFoundException
        assertThrows(com.example.demo.exception.ResourceNotFoundException.class, () -> {
            readingHistoryService.getRecentlyReadArticles(nonExistentUserId);
        });
    }

    @Test
    public void testUpdateIsReadMethod() {
        // Create a reading history with time just below the threshold
        ReadingHistory history = new ReadingHistory();
        history.setUser(testUser);
        history.setArticle(testArticle1);
        history.setTimeSpentSeconds(25);
        history.setLastAccessed(LocalDateTime.now());
        
        // Call the updateIsRead method
        history.updateIsRead();
        
        // Verify it's not marked as read
        assertFalse(history.isRead());
        
        // Update time to exceed threshold
        history.setTimeSpentSeconds(35);
        
        // Call the updateIsRead method again
        history.updateIsRead();
        
        // Verify it's now marked as read
        assertTrue(history.isRead());
    }
}