package com.example.demo.repository;

import com.example.demo.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable; // <-- CORRECT import

public interface ArticleRepository extends JpaRepository<Article, Long> {


    Page<Article> findAll(Pageable pageable);


    List<Article> findByTags_NameIgnoreCase(String tagName);


    // Updated Query: Use 'score' instead of 'averageRating'
    @Query("SELECT a FROM Article a JOIN FETCH a.author " +
            "WHERE a.author.id <> :userId " +
            "ORDER BY a.score DESC, a.createdAt DESC") // ORDER BY score
    List<Article> findTopScoringArticlesExcludingAuthor(
            @Param("userId") Long userId,
            Pageable pageable); // Renamed method for clarity

    // Used for finding candidates based on preferred tags
    @Query("SELECT DISTINCT a FROM Article a JOIN FETCH a.author JOIN a.tags t " +
            "WHERE t.name IN :tagNames " +
            "AND a.id NOT IN :excludedArticleIds " +
            "AND a.author.id <> :authorId " +
            "ORDER BY a.score DESC, a.createdAt DESC") // Ensure sorting
    List<Article> findByTagsAndExcludeIdsAndAuthorOrderByScore(
            @Param("tagNames") Set<String> tagNames,
            @Param("excludedArticleIds") Set<Long> excludedArticleIds,
            @Param("authorId") Long authorId,
            Pageable pageable); // Pageable applies limit



    // NEW: Needed for supplementing recommendations
    @Query("SELECT a FROM Article a JOIN FETCH a.author " +
            "WHERE a.author.id <> :userId " +
            "AND a.id NOT IN :excludedArticleIds " +
            "ORDER BY a.score DESC, a.createdAt DESC")
    List<Article> findTopScoringArticlesExcludingAuthorAndIds(
            @Param("userId") Long userId,
            @Param("excludedArticleIds") Set<Long> excludedArticleIds,
            Pageable pageable);

}


