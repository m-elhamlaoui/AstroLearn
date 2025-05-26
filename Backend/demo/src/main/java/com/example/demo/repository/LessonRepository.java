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

    List<Lesson> findByModuleId(Long moduleId);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    int countByModuleCourseId(@Param("courseId") Long courseId);
    
    // Find lessons by module ID ordered by their sequence
    List<Lesson> findByModuleIdOrderByOrderAsc(Long moduleId);
}