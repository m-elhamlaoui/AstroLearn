package com.example.demo.service;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import java.util.List;
import java.util.Set;

public interface ArticleService {


    ArticleDTO createArticle(ArticleDTO articleDTO, Long authorId);
    ArticleDTO getArticleById(Long id);
    List<ArticleDTO> getAllArticles();


    /*
     * @throws ResourceNotFoundException if article not found
     * @throws UnauthorizedException if user is not author
     */
    ArticleDTO updateArticle(Long id, ArticleDTO articleDTO, Long userId);

    /*
     * Deletes an article. Only author should be allowed.
     * @throws ResourceNotFoundException if article not found
     * @throws UnauthorizedException if user is not author or admin
     */
    void deleteArticle(Long id, Long userId);

    // --- Comments ---

    CommentDTO addComment(Long articleId, CommentDTO commentDTO, Long userId);
    List<CommentDTO> getCommentsByArticleId(Long articleId);

    /*
     * Deletes a comment. Only comment author should be allowed.
     * @throws ResourceNotFoundException if comment not found
     * @throws UnauthorizedException if user is not author or admin
     */
    void deleteComment(Long commentId, Long userId);

    // --- Ratings ---

    ArticleRatingDTO rateArticle(Long articleId, ArticleRatingDTO ratingDTO, Long userId);
    Double getAverageRating(Long articleId);

    // --- Tags ---

    /*
     * Adds tags to an article. Handles creation of new tags if they don't exist.
     * @param articleId Article ID
     * @param tagNames Set of tag names
     * @return Updated ArticleDTO with new tags
     */
    ArticleDTO addTagsToArticle(Long articleId, Set<String> tagNames);

    ArticleDTO removeTagsFromArticle(Long articleId, Set<String> tagNames);
    List<ArticleDTO> getArticlesByTag(String tagName);



    //   Gets recommended articles for a specific user.
    List<ArticleDTO> getRecommendedArticles(Long userId);
}

