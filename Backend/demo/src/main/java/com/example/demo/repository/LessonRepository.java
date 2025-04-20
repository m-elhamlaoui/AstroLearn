package com.example.demo.repository;

import com.example.demo.model.Lesson;
import com.example.demo.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);

    List<Lesson> findByModuleId(Long moduleId);
    // Add this method to find a quiz by lesson ID
    Optional<Quiz> findByLessonId(Long lessonId);
}