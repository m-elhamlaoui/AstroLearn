package com.example.demo.controller;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.service.CourseProgressService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-progress")
@AllArgsConstructor
public class CourseProgressController {

    private final CourseProgressService courseProgressService;


    // Get or create course progress for a user and course
    @GetMapping("/{userId}/{courseId}")
    public ResponseEntity<CourseProgressDTO> getOrCreateCourseProgress(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        CourseProgressDTO progress = courseProgressService.getOrCreateCourseProgress(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    // Mark a lesson as completed
    @PostMapping("/{userId}/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<CourseProgressDTO> markLessonCompleted(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        CourseProgressDTO progress = courseProgressService.markLessonCompleted(userId, courseId, lessonId);
        return ResponseEntity.ok(progress);
    }

    // Get all course progresses for a user
    @GetMapping("/{userId}")
    public ResponseEntity<List<CourseProgressDTO>> getProgressByUserId(@PathVariable Long userId) {
        List<CourseProgressDTO> progressList = courseProgressService.getProgressByUserId(userId);
        return ResponseEntity.ok(progressList);
    }

    // Get progress for a specific user and course
    @GetMapping("/{userId}/{courseId}/progress")
    public ResponseEntity<CourseProgressDTO> getProgressByUserAndCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        CourseProgressDTO progress = courseProgressService.getProgressByUserAndCourse(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    // Set the current lesson for a user in a course
    @PostMapping("/{userId}/{courseId}/lessons/{lessonId}/current")
    public ResponseEntity<CourseProgressDTO> setCurrentLesson(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        CourseProgressDTO progress = courseProgressService.setCurrentLesson(userId, courseId, lessonId);
        return ResponseEntity.ok(progress);
    }

    // Get the current lesson for a user in a course
    @GetMapping("/{userId}/{courseId}/current-lesson")
    public ResponseEntity<Long> getCurrentLesson(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        CourseProgressDTO progress = courseProgressService.getProgressByUserAndCourse(userId, courseId);
        return ResponseEntity.ok(progress.currentLessonId());
    }
}