package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Data


public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password;

    @Email
    @Column(unique = true)
    private String email;

    private String bio;

    private String profileImageUrl;
    private String photoCoverUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private UserVerification isVerified;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CourseProgress> courseProgress = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ArticleRating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();



    @Column(name = "experience_points")
    private int experiencePoints = 0;

    @Enumerated(EnumType.STRING)
    private UserLevel level = UserLevel.NOVICE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReadingHistory> readingHistory = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<QuizCompletion> quizCompletions = new ArrayList<>();



    public void addExperience(int points) {
        this.experiencePoints += points;
        updateLevel();
    }

    private void updateLevel() {
        if (experiencePoints >= UserLevel.GALACTIC.requiredXP) {
            level = UserLevel.GALACTIC;
        } else if (experiencePoints >= UserLevel.ASTRONAUT.requiredXP) {
            level = UserLevel.ASTRONAUT;
        } else if (experiencePoints >= UserLevel.EXPLORER.requiredXP) {
            level = UserLevel.EXPLORER;
        }
    }



    public enum UserRole {
        USER, ADMIN
    }

    public enum UserVerification {
        VERIFIED, UNVERIFIED, PENDING
    }

    public enum UserLevel {
        NOVICE(0),
        EXPLORER(1000),
        ASTRONAUT(5000),
        GALACTIC(10000);

        private final int requiredXP;
        UserLevel(int requiredXP) {
            this.requiredXP = requiredXP;
        }
    }

}
