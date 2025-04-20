package com.example.demo.repository;

import com.example.demo.model.Article;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Set;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByAuthorId(Long authorId);
    List<Article> findByTagsName(String tagName);


    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.name IN :tags GROUP BY a HAVING COUNT(t) = :tagCount")
    List<Article> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount);

    List<Article> findByTags_NameIgnoreCase(String tagName);

    // Find articles containing any of the specified tag names, excluding specific article IDs,
    // and optionally excluding a specific author ID. Sorted by averageRating.
    // Using JOIN FETCH for tags and author to avoid N+1 issues when mapping.
    @Query("SELECT DISTINCT a FROM Article a JOIN FETCH a.tags t JOIN FETCH a.author " +
            "WHERE t.name IN :tagNames " +
            "AND a.id NOT IN :excludedArticleIds " +
            "AND a.author.id <> :userId " + // Exclude user's own articles
            "ORDER BY a.averageRating DESC, a.createdAt DESC")
    List<Article> findArticlesByTagNamesExcludingIdsAndAuthor(
            @Param("tagNames") Set<String> tagNames,
            @Param("excludedArticleIds") Set<Long> excludedArticleIds,
            @Param("userId") Long userId,
            Pageable pageable); // Use Pageable for limiting results

    // Fallback query if no preferred tags are found - just gets top rated excluding user's own
    @Query("SELECT a FROM Article a JOIN FETCH a.author " +
            "WHERE a.author.id <> :userId " +
            "ORDER BY a.averageRating DESC, a.createdAt DESC")
    List<Article> findTopRatedArticlesExcludingAuthor(
            @Param("userId") Long userId,
            Pageable pageable); // Use Pageable for limiting results

    // Find articles by their IDs, fetching necessary associations
    @Query("SELECT a FROM Article a JOIN FETCH a.tags JOIN FETCH a.author WHERE a.id IN :ids")
    List<Article> findByIdInWithTagsAndAuthor(@Param("ids") Set<Long> ids);

}


