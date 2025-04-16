package com.example.demo.repository;

import com.example.demo.model.Article;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByAuthorId(Long authorId);
    List<Article> findByTagsName(String tagName);
    List<Article> findByAuthorIdAndVerifiedTrue(Long authorId);
    List<Article> findByVerifiedTrue();

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.name IN :tags GROUP BY a HAVING COUNT(t) = :tagCount")
    List<Article> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount);


    // Optimizes query: Eagerly fetches 'author' and 'tags' relationships in a single query
    // to prevent the N+1 select problem when accessing these fields later.
    @EntityGraph(attributePaths = {"author", "tags"})
    List<Article> findByVerifiedTrueOrderByCreatedAtDesc();
}


