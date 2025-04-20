package com.example.demo.controller;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.ModuleDTO;
import com.example.demo.dto.LessonDTO;
import com.example.demo.service.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Create a new course
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(createdCourse);
    }

    // Get a course by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Update a course
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    // Delete a course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // Add a module to a course
    @PostMapping("/{courseId}/modules")
    public ResponseEntity<ModuleDTO> addModuleToCourse(@PathVariable Long courseId, @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO createdModule = courseService.addModuleToCourse(courseId, moduleDTO);
        return ResponseEntity.ok(createdModule);
    }

    // Get modules by course ID
    @GetMapping("/{courseId}/modules")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourseId(@PathVariable Long courseId) {
        List<ModuleDTO> modules = courseService.getModulesByCourseId(courseId);
        return ResponseEntity.ok(modules);
    }

    // Add a lesson to a module
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonDTO> addLessonToModule(@PathVariable Long moduleId, @RequestBody LessonDTO lessonDTO) {
        LessonDTO createdLesson = courseService.addLessonToModule(moduleId, lessonDTO);
        return ResponseEntity.ok(createdLesson);
    }

    // Get lessons by module ID
    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<List<LessonDTO>> getLessonsByModuleId(@PathVariable Long moduleId) {
        List<LessonDTO> lessons = courseService.getLessonsByModuleId(moduleId);
        return ResponseEntity.ok(lessons);
    }
}