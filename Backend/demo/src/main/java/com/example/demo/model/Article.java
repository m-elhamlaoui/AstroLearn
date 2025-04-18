package com.example.demo.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String Summary;

    @Lob
    @NotBlank
    private String content;

    private String imageUrl;


    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleRating> ratings = new ArrayList<>();

    @Formula("(SELECT COALESCE(AVG(ar.rating), 0.0) FROM article_ratings ar WHERE ar.article_id = id)")
    private Double averageRating; // Match DTO field

    @Formula("(SELECT COUNT(*) FROM comments c WHERE c.article_id = id)")
    private Long commentCount; // Match DTO field

    // Defines a many-to-many relationship between the Article and ArticleTag entities.
    // The @JoinTable annotation specifies the join table "article_tags" that links the two entities.
    // - "article_id" is the foreign key referencing the Article entity.
    // - "tag_id" is the foreign key referencing the ArticleTag entity.
    // The Set<ArticleTag> ensures no duplicate tags are associated with an Article.

    @ManyToMany
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<ArticleTag> tags = new HashSet<>();

    // Verification fields
    private boolean verified;
    private boolean plagiarismChecked;
    private String verificationNotes;


}



