package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "article_tags")

public class ArticleTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tag_name_id")
    private TagName tagName;


    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    public ArticleTag(Article article, TagName tagName) {
        this.article = article;
        this.tagName = tagName;
    }
}

