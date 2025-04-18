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


    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.name IN :tags GROUP BY a HAVING COUNT(t) = :tagCount")
    List<Article> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount);

}


