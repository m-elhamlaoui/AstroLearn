package com.example.demo.repository;

import com.example.demo.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.example.demo.model.Article.*;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByStatusOrderByCreatedAtDesc(ArticleStatus status);
    List<Article> findByAuthorId(Long authorId);
    List<Article> findByTagsName(String tagName);
    List<Article> findByVerifiedTrue();
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.name IN :tags GROUP BY a HAVING COUNT(t) = :tagCount")
    List<Article> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount);
}