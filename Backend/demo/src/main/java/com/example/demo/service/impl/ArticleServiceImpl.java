package com.example.demo.service.impl;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*; // Import all needed repos
import com.example.demo.service.ArticleService;
//import com.example.demo.service.RecommendationService; // For recommendations
import com.example.demo.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArticleRatingRepository articleRatingRepository;
    private final ArticleTagRepository articleTagRepository;
    private final EntityMapper entityMapper;
    private final RecommendationService recommendationService;


    @Override
    public ArticleDTO createArticle(ArticleDTO articleDTO, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Article article = entityMapper.toEntity(articleDTO);
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        article.setAverageRating(0.0);
        article.setCommentCount(0L);
        // Handle tags if provided during creation
        if (articleDTO.tags() != null && !articleDTO.tags().isEmpty()) {
            Set<ArticleTag> tags = findOrCreateTags(articleDTO.tags());
            article.setTags(tags);
        }


        Article savedArticle = articleRepository.save(article);
        return entityMapper.toDTO(savedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        return entityMapper.toDTO(article);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ArticleDTO> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO, Long userId) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        checkArticlePermissions(existingArticle, userId, "update");

        // Update basic fields from DTO
        entityMapper.updateArticleFromDto(articleDTO, existingArticle);

        // Handle tag updates separately if needed (e.g., clearing and adding)
        if (articleDTO.tags() != null) { // Check if tags were provided in update DTO
            Set<ArticleTag> tags = findOrCreateTags(articleDTO.tags());
            existingArticle.setTags(tags); // Replace existing tags
        }

        // If content is updated, potentially re-trigger verification
        // existingArticle.setVerified(false); // Mark as unverified on update? Decision needed.
        // contentVerificationService.verifyArticleContent(existingArticle.getId());

        Article updatedArticle = articleRepository.save(existingArticle);
        return entityMapper.toDTO(updatedArticle);
    }

    @Override
    public void deleteArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        checkArticlePermissions(article, userId, "delete");
        articleRepository.delete(article); // Cascade should handle comments, ratings etc.
    }

    
    // --- Comments Implementation ---
    @Override
    public CommentDTO addComment(Long articleId, CommentDTO commentDTO, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = entityMapper.toEntity(commentDTO);
        comment.setArticle(article);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now()); // Or rely on @PrePersist

        Comment savedComment = commentRepository.save(comment);
        return entityMapper.toDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByArticleId(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Article", "id", articleId);
        }
        // Assuming findByArticleId method exists in CommentRepository
        List<Comment> comments = commentRepository.findByArticleId(articleId);
        return comments.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        checkCommentPermissions(comment, userId, "delete");

        commentRepository.delete(comment);
    }


    // --- Ratings Implementation ---
    @Override
    public ArticleRatingDTO rateArticle(Long articleId, ArticleRatingDTO ratingDTO, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Find existing rating or create a new one
        Optional<ArticleRating> existingRatingOpt = articleRatingRepository.findByUserIdAndArticleId(userId, articleId);

        ArticleRating rating;
        if (existingRatingOpt.isPresent()) {
            rating = existingRatingOpt.get();
            // Update existing rating
            rating.setRating(ratingDTO.rating());
        } else {
            // Create new rating
            rating = new ArticleRating(); // Don't use mapper for partial DTO -> Entity creation here
            rating.setArticle(article);
            rating.setUser(user);
            rating.setRating(ratingDTO.rating());
        }

        ArticleRating savedRating = articleRatingRepository.save(rating);

        // Note: The averageRating on ArticleDTO relies on the @Formula field.
        // No need to manually recalculate here unless you remove the @Formula.

        return entityMapper.toDTO(savedRating);
    }


    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        // Rely on the @Formula field calculated by Hibernate/JPA
        return article.getAverageRating() != null ? article.getAverageRating() : 0.0;
    }


    // --- Tags Implementation ---
    @Override
    public ArticleDTO addTagsToArticle(Long articleId, Set<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        // TODO: Security check if needed (e.g., only author/admin can add tags)

        Set<ArticleTag> tags = findOrCreateTags(tagNames);
        article.getTags().addAll(tags); // Add the new tags

        Article savedArticle = articleRepository.save(article);
        return entityMapper.toDTO(savedArticle);
    }

    @Override
    public ArticleDTO removeTagsFromArticle(Long articleId, Set<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        // TODO: Security check if needed

        if (tagNames == null || tagNames.isEmpty()) {
            return entityMapper.toDTO(article); // No tags to remove
        }

        // Remove only the tags specified
        article.getTags().removeIf(tag -> tagNames.contains(tag.getName()));

        Article savedArticle = articleRepository.save(article);
        return entityMapper.toDTO(savedArticle);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesByTag(String tagName) {
        List<Article> articles = articleRepository.findByTags_NameIgnoreCase(tagName);

        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        return articles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }



    // --- Recommendation --- Placeholder
    @Override
    @Transactional(readOnly = true)
    public List<ArticleDTO> getRecommendedArticles(Long userId) {
        System.out.println("Placeholder: Fetching recommendations for user " + userId);
        // Delegate to RecommendationService
        return recommendationService.getArticleRecommendationsForUser(userId, 10); // Example count
    }


    // --- Helper Methods ---
    private Set<ArticleTag> findOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }
        return tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase) // Normalize tag names
                .map(name -> articleTagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> articleTagRepository.save(new ArticleTag(null, name, Collections.emptySet())))) // Create if not exists
                .collect(Collectors.toSet());
    }

    private void checkArticlePermissions(Article article, Long userId, String action) {
        // TODO: Implement security check using Spring Security
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAuthor = article.getAuthor() != null && article.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("User not authorized to " + action + " this article.");
        }
        System.out.println("Placeholder: Permissions check passed for user " + userId + " to " + action + " article " + article.getId());
    }

    private void checkCommentPermissions(Comment comment, Long userId, String action) {
        // TODO: Implement security check using Spring Security
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAuthor = comment.getUser() != null && comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("User not authorized to " + action + " this comment.");
        }
        System.out.println("Placeholder: Permissions check passed for user " + userId + " to " + action + " comment " + comment.getId());
    }
}
