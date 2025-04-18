package com.example.demo.model; // Adjust package if needed

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*; // Import specific Lombok annotations
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For equals/hashCode implementation

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"articles", "courseProgress", "ratings", "comments", "readingHistory", "quizCompletions", "spaceMissions", "password"}) // Exclude collections and password from toString to prevent loops/leaks
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Base equals/hashCode only on ID
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @Lob
    private String bio;

    private String profileImageUrl;
    private String photoCoverUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole role = UserRole.USER; // Default role

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserVerification verificationStatus = UserVerification.UNVERIFIED; // Default status

    // --- Relationships with Cascade Deletion & Orphan Removal ---

    // Articles are deleted when the user is deleted
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();

    // Course Progress is user-specific and deleted with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseProgress> courseProgress = new ArrayList<>();

    // Ratings are user-specific and deleted with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleRating> ratings = new ArrayList<>();

    // Comments are user-specific and deleted with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Reading History is user-specific and deleted with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingHistory> readingHistory = new ArrayList<>();

    // Quiz Completions are user-specific and deleted with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizCompletion> quizCompletions = new ArrayList<>();

    // Space Missions created by the user are deleted when the user is deleted (as per decision)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceMission> spaceMissions = new ArrayList<>();


    // --- Calculated Fields ---

    @Formula("(SELECT COUNT(*) FROM articles a WHERE a.author_id = id)")
    private Long articleCount;

    @Formula("(SELECT COUNT(*) FROM comments c WHERE c.user_id = id)")
    private Long commentCount;

    @Formula("(SELECT COUNT(*) FROM quiz_completions qc WHERE qc.user_id = id)")
    private Long quizCompletionCount;


    // --- Experience & Level ---

    @Column(name = "experience_points", nullable = false)
    private int experiencePoints = 0;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserLevel level = UserLevel.NOVICE;


    // --- Methods ---

    public void addExperience(int points) {
        if (points > 0) { // Basic validation
            this.experiencePoints += points;
            updateLevel();
        }
    }

    private void updateLevel() {
        // Iterate downwards to set the highest applicable level
        if (experiencePoints >= UserLevel.GALACTIC.requiredXP) {
            level = UserLevel.GALACTIC;
        } else if (experiencePoints >= UserLevel.ASTRONAUT.requiredXP) {
            level = UserLevel.ASTRONAUT;
        } else if (experiencePoints >= UserLevel.EXPLORER.requiredXP) {
            level = UserLevel.EXPLORER;
        } else {
            level = UserLevel.NOVICE; // Ensure reset if XP somehow drops below EXPLORER
        }
    }

    // --- Enums ---
    // (It's often better practice to move Enums to their own files in a sub-package e.g., com.example.demo.model.enums)
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

        // Make requiredXP public if needed elsewhere, or provide a getter
        public final int requiredXP;

        UserLevel(int requiredXP) {
            this.requiredXP = requiredXP;
        }

        public int getRequiredXP() {
            return requiredXP;
        }
    }

    // --- equals() and hashCode() ---
    // Based only on ID for JPA best practices

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // If ID is null, objects are only equal if they are the same instance
        // If ID is not null, compare IDs
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        // Use a constant for objects with null ID, or the ID's hashcode
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
}