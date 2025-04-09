package com.example.demo.repository;

import com.example.demo.model.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

    Optional<ArticleTag> findByName(String name);
    List<ArticleTag> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t.name, COUNT(a) FROM ArticleTag t JOIN t.articles a GROUP BY t.name ORDER BY COUNT(a) DESC")
    List<Object[]> findPopularTags();
}
