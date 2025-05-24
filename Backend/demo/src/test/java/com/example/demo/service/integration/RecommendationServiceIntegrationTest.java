//package com.example.demo.service.integration;
//
//import com.example.demo.dto.ArticleDTO;
//import com.example.demo.model.Article;
//import com.example.demo.model.User;
//import com.example.demo.repository.ArticleRepository;
//import com.example.demo.repository.ArticleVoteRepository;
//import com.example.demo.repository.ReadingHistoryRepository;
//import com.example.demo.repository.UserRepository;
//import com.example.demo.service.RecommendationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//public class RecommendationServiceIntegrationTest {
//
//    @Autowired
//    private RecommendationService recommendationService;
//
//    @Autowired
//    private ArticleRepository articleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ReadingHistoryRepository readingHistoryRepository;
//
//    @Autowired
//    private ArticleVoteRepository articleVoteRepository;
//
//    private User testUser;
//    private Article testArticle;
//
//    @BeforeEach
//    void setUp() {
//        // Clean up before each test
//        articleVoteRepository.deleteAll();
//        readingHistoryRepository.deleteAll();
//        articleRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // Create test user
//        testUser = new User();
//        testUser.setUsername("testuser");
//        testUser.setEmail("test@example.com");
//        testUser.setPassword("password");
//        testUser = userRepository.save(testUser);
//
//        // Create test article
//        testArticle = new Article();
//        testArticle.setTitle("Test Article");
//        testArticle.setContent("Test Content");
//        testArticle.setAuthor(testUser);
//        testArticle.setScore(10);
//        testArticle = articleRepository.save(testArticle);
//    }
//
//    @Test
//    void testGetArticleRecommendationsForUser() {
//        List<ArticleDTO> recommendations = recommendationService.getArticleRecommendationsForUser(testUser.getId(), 5);
//        assertThat(recommendations).isNotNull();
//        assertThat(recommendations).hasSize(1);
//        assertThat(recommendations.get(0).title()).isEqualTo("Test Article");
//    }
//
//    @Test
//    void testGetPopularArticles() {
//        List<ArticleDTO> popularArticles = recommendationService.getPopularArticles(5);
//        assertThat(popularArticles).isNotNull();
//        assertThat(popularArticles).hasSize(1);
//        assertThat(popularArticles.get(0).title()).isEqualTo("Test Article");
//    }
//}