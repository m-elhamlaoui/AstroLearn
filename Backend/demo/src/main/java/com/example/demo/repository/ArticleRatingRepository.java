package com.example.demo.repository;

import com.example.demo.model.ArticleRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRatingRepository extends JpaRepository<ArticleRating, Long> {
    Optional<ArticleRating> findByArticleIdAndUserId(Long articleId, Long userId);

    // find average rating for a specific article
    @Query("SELECT AVG(r.rating) FROM ArticleRating r WHERE r.article.id = :articleId")
    Double findAverageRatingByArticleId(@Param("articleId") Long articleId);

    // find total number of ratings for a specific article
    @Query("SELECT COUNT(r) FROM ArticleRating r WHERE r.article.id = :articleId")
    Long countByArticleId(@Param("articleId") Long articleId);
}
