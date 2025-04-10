package com.example.demo.mapper;

import com.example.demo.dto.LessonDTO;
import com.example.demo.dto.QuizDTO; // Make sure this DTO exists
import com.example.demo.model.Lesson;
import com.example.demo.model.Quiz;   // Make sure this Entity exists
// Import other necessary DTOs and Entities as you add more mappings
// import com.example.demo.model.Module; // Might be needed depending on context

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Configures MapStruct to generate a Spring Bean
public interface EntityMapper {

    // ==================== Lesson Mapping ====================

    /**
     * Maps a Lesson Entity to a LessonDTO.
     * - module relationship is flattened to moduleId.
     * - quiz relationship is mapped to QuizDTO (requires a corresponding Quiz <-> QuizDTO mapping).
     */
    @Mapping(target = "moduleId", source = "module.id") // Correctly maps Module object's ID to moduleId field
    // MapStruct implicitly maps fields with the same name and type (id, title, content, videoUrl).
    // MapStruct will implicitly try to map lesson.quiz (Quiz object) to lessonDTO.quiz (QuizDTO object)
    // IF a method 'QuizDTO toDTO(Quiz quiz)' exists in this interface (or a 'used' mapper).
    LessonDTO toDTO(Lesson lesson);

    /**
     * Maps a LessonDTO to a Lesson Entity.
     * - Ignores 'module' mapping (needs to be handled in service layer to fetch and set the actual Module entity).
     * - Ignores 'quiz' mapping (needs to be handled in service layer to fetch/create and set the actual Quiz entity).
     */
    @Mapping(target = "module", ignore = true) // Correct: Ignore complex object mapping - handle relationship in Service
    @Mapping(target = "quiz", ignore = true)   // Correct: Ignore complex object mapping - handle relationship in Service
    // MapStruct implicitly maps fields with the same name and type (id, title, content, videoUrl).
    Lesson toEntity(LessonDTO lessonDTO);


    // ==================== Placeholder for Quiz Mapping (Add next) ====================
    /*
    @Mapping(target = "lessonId", source = "lesson.id") // Example mapping for Quiz -> QuizDTO
    QuizDTO toDTO(Quiz quiz);

    @Mapping(target = "lesson", ignore = true) // Example mapping for QuizDTO -> Quiz
    Quiz toEntity(QuizDTO quizDTO);
    */


    // ==================== Add other entity/DTO mappings below ====================
    // ... e.g., for Module, Course, User, etc.

}