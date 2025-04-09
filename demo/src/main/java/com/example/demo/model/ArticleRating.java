package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_ratings")
public class ArticleRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    private int rating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    private LocalDateTime ratedAt;

    // Constructors, getters, setters
    @PrePersist
    protected void onCreate() {
        this.ratedAt = LocalDateTime.now();
    }
}
