package com.example.demo.repository;

import com.example.demo.model.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDifficulty(Course.DifficultyLevel difficulty);
    List<Course> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = {"modules"})
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.modules m")
    List<Course> findAllWithModules();
}
