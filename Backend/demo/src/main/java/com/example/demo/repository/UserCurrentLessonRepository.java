package com.example.demo.repository;

import com.example.demo.model.UserCurrentLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCurrentLessonRepository extends JpaRepository<UserCurrentLesson, Long> {
    
    // Find the current lesson for a user in a course
    Optional<UserCurrentLesson> findByUserIdAndCourseId(Long userId, Long courseId);
    
    // Check if a current lesson record exists for a user in a course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
