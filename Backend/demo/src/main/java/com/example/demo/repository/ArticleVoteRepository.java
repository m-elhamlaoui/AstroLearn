package com.example.demo.repository;

import com.example.demo.model.ArticleVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ArticleVoteRepository extends JpaRepository<ArticleVote, Long> {

    // Find a specific vote by user and article
    Optional<ArticleVote> findByUserIdAndArticleId(Long userId, Long articleId);

    // Find all votes by a user for a given list of article IDs
    List<ArticleVote> findByUserIdAndArticleIdIn(Long userId, List<Long> articleIds);

    // Find all votes for a specific article
    List<ArticleVote> findByArticleId(Long articleId);

    // Optional: Find all article IDs downvoted by a specific user
    @Query("SELECT av.article.id FROM ArticleVote av WHERE av.user.id = :userId AND av.value = -1")
    Set<Long> findDownvotedArticleIdsByUserId(Long userId);

    // Counts are now handled by @Formula on Article entity, so explicit count/sum queries here
    // are less necessary unless needed for specific bulk operations.
    @Query("SELECT av.article.id FROM ArticleVote av WHERE av.user.id = :userId AND av.value = 1")
    Set<Long> findUpvotedArticleIdsByUserId(@Param("userId") Long userId);
}
