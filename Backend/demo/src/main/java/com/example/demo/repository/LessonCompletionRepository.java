package com.example.demo.repository;

import com.example.demo.model.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    
    // Find all completions for a specific user
    List<LessonCompletion> findByUserId(Long userId);
    
    // Find all completions for a specific course
    List<LessonCompletion> findByCourseId(Long courseId);
    
    // Find all completions for a specific user in a specific course
    List<LessonCompletion> findByUserIdAndCourseId(Long userId, Long courseId);
    
    // Check if a specific lesson is completed by a user
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
    
    // Find a specific completion by user and lesson
    Optional<LessonCompletion> findByUserIdAndLessonId(Long userId, Long lessonId);
    
    // Count completed lessons for a user in a course
    @Query("SELECT COUNT(lc) FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.course.id = :courseId")
    long countByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    // Get all lesson IDs completed by a user for a course
    @Query("SELECT lc.lesson.id FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.course.id = :courseId")
    List<Long> findCompletedLessonIdsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
