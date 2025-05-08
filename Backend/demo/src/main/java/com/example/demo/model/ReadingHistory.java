package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
@Table(name = "reading_history")
public class ReadingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    private boolean isRead;
    private int timeSpentSeconds;
    private LocalDateTime lastAccessed;

    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
        updateIsRead();
    }

    private void updateIsRead() {
        this.isRead = this.timeSpentSeconds >= 300;
    }

    public void addTimeSpent(int additionalSeconds) {
        this.timeSpentSeconds += additionalSeconds;
        this.lastAccessed = LocalDateTime.now();
        updateIsRead();
    }
}