package com.example.demo.service;
import java.util.List;
import com.example.demo.dto.ArticleDTO;

public interface RecommendationService {
    /**
     * Gets personalized article recommendations for a user.
     * Implementation would involve analyzing reading history, ratings, user profile etc.
     * @param userId The ID of the user to get recommendations for.
     * @param count The maximum number of recommendations to return.
     * @return A list of recommended ArticleDTOs.
     */
    List<ArticleDTO> getArticleRecommendationsForUser(Long userId, int count);

    /**
     * Gets generally popular or trending articles.
     * @param count The maximum number of articles to return.
     * @return A list of popular ArticleDTOs.
     */
    List<ArticleDTO> getPopularArticles(int count);

    // Could add course recommendations here too
}
