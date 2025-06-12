package com.example.demo.controller;

import com.example.demo.dto.LessonDTO;
import com.example.demo.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    // Add a lesson to a module
    @PostMapping("/modules/{moduleId}")
    public ResponseEntity<LessonDTO> addLessonToModule(@PathVariable Long moduleId, @RequestBody LessonDTO lessonDTO) {
        LessonDTO createdLesson = lessonService.addLessonToModule(moduleId, lessonDTO);
        return ResponseEntity.ok(createdLesson);
    }

    // Get lessons by module ID
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByModuleId(@PathVariable Long moduleId) {
        List<LessonDTO> lessons = lessonService.getLessonsByModuleId(moduleId);
        return ResponseEntity.ok(lessons);
    }

    // Get a lesson by ID
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long lessonId) {
        LessonDTO lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    // Update a lesson
    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long lessonId, @RequestBody LessonDTO lessonDTO) {
        LessonDTO updatedLesson = lessonService.updateLesson(lessonId, lessonDTO);
        return ResponseEntity.ok(updatedLesson);
    }

    // Delete a lesson
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
}