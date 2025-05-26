package com.example.demo.repository;

import com.example.demo.model.ReadingHistory;

import java.util.List;
import java.util.Optional;
import com.example.demo.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
// import set
import java.util.Set;


public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    Optional<ReadingHistory> findByUserAndArticle(User user, Article article);
    List<ReadingHistory> findByUserOrderByLastAccessedDesc(User user);

    // Return all articles in reading history, ordered by most recently accessed
    @Query("SELECT rh.article FROM ReadingHistory rh WHERE rh.user.id = :userId ORDER BY rh.lastAccessed DESC")
    List<Article> findRecentlyReadArticles(@Param("userId") Long userId);


    // Find history for a user where article is considered 'read' (adjust threshold)
    // Fetches the associated Article ID efficiently
    @Query("SELECT rh.article.id FROM ReadingHistory rh WHERE rh.user.id = :userId AND rh.isRead = true") // Or use timeSpentSeconds > threshold
    Set<Long> findReadArticleIdsByUserId(@Param("userId") Long userId);
}

