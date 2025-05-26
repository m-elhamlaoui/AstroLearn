package com.example.demo.service;

import com.example.demo.dto.CourseProgressSummaryDTO;
import com.example.demo.dto.LessonCompletionDTO;

import java.util.List;

public interface LessonCompletionService {
    
    /**
     * Mark a lesson as completed by a user
     * @param userId User ID
     * @param lessonId Lesson ID
     * @param courseId Course ID
     * @return The created LessonCompletionDTO
     */
    LessonCompletionDTO markLessonCompleted(Long userId, Long lessonId, Long courseId);
    
    /**
     * Get all lesson completions for a user
     * @param userId User ID
     * @return List of LessonCompletionDTO
     */
    List<LessonCompletionDTO> getUserCompletions(Long userId);
    
    /**
     * Get all lesson completions for a user in a specific course
     * @param userId User ID
     * @param courseId Course ID
     * @return List of LessonCompletionDTO
     */
    List<LessonCompletionDTO> getUserCourseCompletions(Long userId, Long courseId);
    
    /**
     * Check if a lesson is completed by a user
     * @param userId User ID
     * @param lessonId Lesson ID
     * @return true if completed, false otherwise
     */
    boolean isLessonCompleted(Long userId, Long lessonId);
    
    /**
     * Get a summary of course progress for a user
     * @param userId User ID
     * @param courseId Course ID
     * @return CourseProgressSummaryDTO
     */
    CourseProgressSummaryDTO getCourseProgressSummary(Long userId, Long courseId);
    
    /**
     * Set the current lesson a user is viewing in a course
     * This doesn't mark the lesson as completed, just tracks what the user is currently viewing
     * @param userId User ID
     * @param courseId Course ID
     * @param lessonId Lesson ID
     * @return Updated CourseProgressSummaryDTO
     */
    CourseProgressSummaryDTO setCurrentLesson(Long userId, Long courseId, Long lessonId);
}
