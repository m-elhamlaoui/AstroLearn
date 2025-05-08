package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "article_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "article_id" }) // Ensure one vote per user per article
})
public class ArticleVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "vote_value", nullable = false)
    private int value; // +1 for upvote, -1 for downvote

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is usually better here
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    // Helper to check if it's an upvote
    @Transient // Not persisted
    public boolean isUpvote() {
        return this.value > 0;
    }

    // Helper to check if it's a downvote
    @Transient // Not persisted
    public boolean isDownvote() {
        return this.value < 0;
    }
}