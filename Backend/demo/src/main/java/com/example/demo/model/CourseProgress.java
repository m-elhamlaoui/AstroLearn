package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "course_progress")
public class CourseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_lesson_id")
    private Lesson currentLesson;

    @ElementCollection
    @CollectionTable(name = "completed_lessons", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "lesson_id")
    private Set<Long> completedLessonIds = new HashSet<>();

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completion_percentage", nullable = false)
    private double completionPercentage;

    @Column(name = "last_accessed", nullable = false)
    private LocalDateTime lastAccessed;
}

