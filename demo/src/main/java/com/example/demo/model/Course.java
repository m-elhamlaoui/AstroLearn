package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "courses")

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Module> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseProgress> progresses = new ArrayList<>();

    @Transient // Calculated field, not persisted
    public int getTotalLessons() {
        return modules.stream()
                .mapToInt(module -> module.getLessons().size())
                .sum();
    }
}

 enum DifficultyLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

