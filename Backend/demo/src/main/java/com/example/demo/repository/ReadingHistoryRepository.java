package com.example.demo.repository;

import com.example.demo.model.ReadingHistory;

import java.util.List;
import java.util.Optional;
import com.example.demo.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    Optional<ReadingHistory> findByUserAndArticle(User user, Article article);
    List<ReadingHistory> findByUserOrderByLastAccessedDesc(User user);

    @Query("SELECT rh.article FROM ReadingHistory rh WHERE rh.user.id = :userId ORDER BY rh.lastAccessed DESC")
    List<Article> findRecentlyReadArticles(@Param("userId") Long userId);
}

