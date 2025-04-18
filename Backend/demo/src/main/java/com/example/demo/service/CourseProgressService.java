package com.example.demo.service;

import com.example.demo.dto.CourseProgressDTO;
import java.util.List;

public interface CourseProgressService {

    /*
     * Gets or creates the progress record for a user and a course.
     * If progress doesn't exist, it initializes it. Updates lastAccessed time.
     * @param userId User ID
     * @param courseId Course ID
     */
    CourseProgressDTO getOrCreateCourseProgress(Long userId, Long courseId);

    /*
     * Marks a lesson as completed for a user within a course.
     * Updates completion percentage and potentially marks course as completed.
     * Sets the 'currentLesson' to the next logical lesson if possible (optional enhancement).
     * @param lessonId Lesson ID to mark complete
     * @return Updated CourseProgressDTO
     */
    CourseProgressDTO markLessonCompleted(Long userId, Long courseId, Long lessonId);

    /*
     * Gets all course progresses for a specific user.
     * @param userId User ID
     * @return List of CourseProgressDTOs
     */
    List<CourseProgressDTO> getProgressByUserId(Long userId);

    /*
     * Gets the progress for a specific user and course.
     * @param userId User ID
     * @param courseId Course ID
     * @return CourseProgressDTO
     * @throws ResourceNotFoundException if progress not found
     */
    CourseProgressDTO getProgressByUserAndCourse(Long userId, Long courseId);

    /*
     * Sets the current lesson a user is viewing in a course.
     * @param userId User ID
     * @param courseId Course ID
     * @param lessonId Current Lesson ID
     * @return Updated CourseProgressDTO
     */
    CourseProgressDTO setCurrentLesson(Long userId, Long courseId, Long lessonId);

}

