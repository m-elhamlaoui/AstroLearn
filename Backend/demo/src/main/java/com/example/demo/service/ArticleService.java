package com.example.demo.service;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import com.example.demo.model.VoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ArticleService {


    ArticleDTO createArticle(ArticleDTO articleDTO, Long authorId);
    // Add userId parameter to fetch vote status for the specific user
    ArticleDTO getArticleById(Long id, Long userId); 
    // Add userId parameter
    Page<ArticleDTO> getAllArticles(Pageable pageable, Long userId); 


    @Transactional(readOnly = true)
    // Add userId parameter
    Page<ArticleDTO> getAllArticlesSorted(Pageable pageable, Long userId); 

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

     ArticleDTO voteArticle(Long articleId, Long userId, ArticleVoteRequestDTO voteRequest); // New method
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

    List<CommentDTO> getCommentsByUserId(Long userId);

    // Fetch articles published by a specific user
    List<ArticleDTO> getArticlesByAuthorId(Long authorId, Long currentUserId); // Pass currentUserId for vote status

    // Fetch articles voted on by a specific user
    List<ArticleDTO> getVotedArticlesByUserId(Long userId, VoteType voteType, Long currentUserId); // Pass currentUserId for vote status
    
    // Fetch multiple articles by their IDs
    List<ArticleDTO> getArticlesByIds(List<Long> ids, Long currentUserId); // Pass currentUserId for vote status
}
