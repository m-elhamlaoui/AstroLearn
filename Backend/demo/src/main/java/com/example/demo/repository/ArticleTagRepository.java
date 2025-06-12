package com.example.demo.repository;

import com.example.demo.model.Article;
import com.example.demo.model.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {


    Optional<ArticleTag> findByArticleAndTagName_NameIgnoreCase(Article article, String tagName);


}
