package com.example.demo.repository;

import com.example.demo.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);
    List<Comment> findByUserId(Long userId);
    long countByArticleId(Long articleId);

    // delete all comments for a specific article
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);
}