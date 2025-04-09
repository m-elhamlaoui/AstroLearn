package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course_progress")
public class CourseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "current_lesson_id")
    private Lesson currentLesson;

    private double completionPercentage;

    private boolean completed;

    @ElementCollection
    @CollectionTable(
            name = "completed_lessons",
            joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "lesson_id")
    private Set<Long> completedLessonIds = new HashSet<>();

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    // Constructors, getters, setters
    @PreUpdate
    @PrePersist
    public void updateCompletion() {
        if (course != null && course.getTotalLessons() > 0) {
            this.completionPercentage =
                    ((double) completedLessonIds.size() / course.getTotalLessons()) * 100;
            this.completed = completionPercentage >= 100;
        }
    }
}
