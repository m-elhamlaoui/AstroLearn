package com.example.demo.controller;

import com.example.demo.dto.CourseProgressSummaryDTO;
import com.example.demo.dto.LessonCompletionDTO;
import com.example.demo.service.LessonCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-progress")
@RequiredArgsConstructor
public class LessonCompletionController {

    private final LessonCompletionService lessonCompletionService;

    @PostMapping("/{userId}/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<LessonCompletionDTO> markLessonCompleted(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        
        LessonCompletionDTO completion = lessonCompletionService.markLessonCompleted(userId, lessonId, courseId);
        return new ResponseEntity<>(completion, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/completions")
    public ResponseEntity<List<LessonCompletionDTO>> getUserCompletions(
            @PathVariable Long userId) {
        
        List<LessonCompletionDTO> completions = lessonCompletionService.getUserCompletions(userId);
        return ResponseEntity.ok(completions);
    }

    @GetMapping("/{userId}/{courseId}/completions")
    public ResponseEntity<List<LessonCompletionDTO>> getUserCourseCompletions(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        
        List<LessonCompletionDTO> completions = lessonCompletionService.getUserCourseCompletions(userId, courseId);
        return ResponseEntity.ok(completions);
    }

    @GetMapping("/{userId}/lessons/{lessonId}/completed")
    public ResponseEntity<Boolean> isLessonCompleted(
            @PathVariable Long userId,
            @PathVariable Long lessonId) {
        
        boolean completed = lessonCompletionService.isLessonCompleted(userId, lessonId);
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/{userId}/{courseId}/progress")
    public ResponseEntity<CourseProgressSummaryDTO> getCourseProgressSummary(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        
        CourseProgressSummaryDTO progress = lessonCompletionService.getCourseProgressSummary(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/{userId}/{courseId}/lessons/{lessonId}/current")
    public ResponseEntity<CourseProgressSummaryDTO> setCurrentLesson(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        
        CourseProgressSummaryDTO progress = lessonCompletionService.setCurrentLesson(userId, courseId, lessonId);
        return ResponseEntity.ok(progress);
    }
}
