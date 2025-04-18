package com.example.demo.service;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.ModuleDTO;
import com.example.demo.dto.LessonDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.model.Course; // For Enum

import java.util.List;

public interface CourseService {


    CourseDTO createCourse(CourseDTO courseDTO); // Add userId param if creator tracking needed
    CourseDTO getCourseById(Long id);
    List<CourseDTO> getAllCourses();
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    void deleteCourse(Long id); // Deletes a course (admin function). Cascades to modules and lessons.


    // --- Module Operations ---


    ModuleDTO addModuleToCourse(Long courseId, ModuleDTO moduleDTO);
    ModuleDTO getModuleById(Long moduleId);
    ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO);     // Updates a module (admin function)
    void deleteModule(Long moduleId);      // Deletes a module (admin function). Cascades to lessons.
    List<ModuleDTO> getModulesByCourseId(Long courseId);


    // --- Lesson Operations ---

    LessonDTO addLessonToModule(Long moduleId, LessonDTO lessonDTO); // Adds a lesson to a module (admin function)
    LessonDTO getLessonById(Long lessonId);
    LessonDTO updateLesson(Long lessonId, LessonDTO lessonDTO); // Updates a lesson (admin function)
    void deleteLesson(Long lessonId); // Deletes a lesson (admin function). Cascades to quizzes.
    List<LessonDTO> getLessonsByModuleId(Long moduleId);


    // --- Quiz Operations ---

    QuizDTO addOrUpdateQuizForLesson(Long lessonId, QuizDTO quizDTO); // Adds or updates a quiz for a lesson (admin function)
    QuizDTO getQuizByLessonId(Long lessonId);
    void deleteQuizByLessonId(Long lessonId); // Deletes a quiz by lesson ID (admin function)

}