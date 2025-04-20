package com.example.demo.service.impl;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.LessonDTO;
import com.example.demo.dto.ModuleDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.model.Module;
import com.example.demo.repository.*;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final EntityMapper entityMapper;

    // --- Course Methods ---
    @Override
    public CourseDTO createCourse(CourseDTO courseDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for creating course");

        Course course = entityMapper.toEntity(courseDTO);
        course.setModules(Collections.emptyList());
        course.setProgresses(Collections.emptyList());
        Course savedCourse = courseRepository.save(course);
        return entityMapper.toDTO(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return entityMapper.toDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for updating course " + id);

        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Update basic fields manually or via mapper update method if defined
        existingCourse.setTitle(courseDTO.title());
        existingCourse.setDescription(courseDTO.description());
        existingCourse.setDifficulty(courseDTO.difficulty());

        Course updatedCourse = courseRepository.save(existingCourse);
        return entityMapper.toDTO(updatedCourse);
    }

    @Override
    public void deleteCourse(Long id /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for deleting course " + id);

        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }
        courseRepository.deleteById(id); // Cascade handles modules, lessons, etc.
    }


    // --- Module Methods ---
    @Override
    public ModuleDTO addModuleToCourse(Long courseId, ModuleDTO moduleDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for adding module to course " + courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        Module module = entityMapper.toEntity(moduleDTO);
        module.setCourse(course);
        module.setLessons(Collections.emptyList());
        Module savedModule = moduleRepository.save(module);
        return entityMapper.toDTO(savedModule);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleDTO getModuleById(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        return entityMapper.toDTO(module);
    }

    @Override
    public ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for updating module " + moduleId);

        Module existingModule = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        existingModule.setTitle(moduleDTO.title());
        Module updatedModule = moduleRepository.save(existingModule);
        return entityMapper.toDTO(updatedModule);
    }

    @Override
    public void deleteModule(Long moduleId /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for deleting module " + moduleId);

        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }
        moduleRepository.deleteById(moduleId); // Cascade handles lessons
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        return moduleRepository.findByCourseId(courseId).stream() // Assumes findByCourseId exists
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }


    // --- Lesson Methods ---
    @Override
    public LessonDTO addLessonToModule(Long moduleId, LessonDTO lessonDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for adding lesson to module " + moduleId);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        Lesson lesson = entityMapper.toEntity(lessonDTO);
        lesson.setModule(module);
        lesson.setQuiz(null); // Quiz added separately
        Lesson savedLesson = lessonRepository.save(lesson);
        return entityMapper.toDTO(savedLesson);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        return entityMapper.toDTO(lesson);
    }

    @Override
    public LessonDTO updateLesson(Long lessonId, LessonDTO lessonDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for updating lesson " + lessonId);

        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        existingLesson.setTitle(lessonDTO.title());
        existingLesson.setContent(lessonDTO.content());
        existingLesson.setVideoUrl(lessonDTO.videoUrl());
        Lesson updatedLesson = lessonRepository.save(existingLesson);
        return entityMapper.toDTO(updatedLesson);
    }

    @Override
    public void deleteLesson(Long lessonId /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for deleting lesson " + lessonId);

        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        lessonRepository.deleteById(lessonId); // Cascade handles quiz
    }


    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByModuleId(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }
        return lessonRepository.findByModuleId(moduleId).stream()
                .map(entityMapper::toDTO) // Explicitly map Lesson to LessonDTO
                .collect(Collectors.toList());
    }



    // --- Quiz Methods ---
    @Override
    public QuizDTO addOrUpdateQuizForLesson(Long lessonId, QuizDTO quizDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for adding/updating quiz for lesson " + lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        Quiz quiz;
        Optional<Quiz> existingQuizOpt = quizRepository.findByLessonId(lessonId); // Assumes findByLessonId exists

        if (existingQuizOpt.isPresent()) {
            quiz = existingQuizOpt.get();
            quiz.setTitle(quizDTO.title());
            quiz.setExperienceReward(quizDTO.experienceReward());
            // Efficiently replace questions: delete existing, then add new
            quizQuestionRepository.deleteAll(quiz.getQuestions()); // Or let orphanRemoval handle if configured
            quiz.getQuestions().clear();
        } else {
            quiz = new Quiz();
            quiz.setLesson(lesson);
            quiz.setTitle(quizDTO.title());
            quiz.setExperienceReward(quizDTO.experienceReward());
        }

        Quiz savedQuiz = quizRepository.save(quiz); // Save quiz first

        // Add questions
        if (quizDTO.questions() != null) {
            List<QuizQuestion> questions = quizDTO.questions().stream()
                    .map(qDto -> {
                        QuizQuestion question = entityMapper.toEntity(qDto);
                        question.setQuiz(savedQuiz); // Link to saved quiz
                        return question;
                    }).collect(Collectors.toList());
            quizQuestionRepository.saveAll(questions); // Save questions
            savedQuiz.setQuestions(questions); // Update collection in memory
        } else {
            savedQuiz.setQuestions(Collections.emptyList());
        }

        // Link quiz back to lesson if new
        if (existingQuizOpt.isEmpty()) {
            lesson.setQuiz(savedQuiz);
            lessonRepository.save(lesson);
        }

        return entityMapper.toDTO(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizByLessonId(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        Quiz quiz = quizRepository.findByLessonId(lessonId) // Assumes findByLessonId exists
                .orElseThrow(() -> new ResourceNotFoundException("Quiz for Lesson", "lessonId", lessonId));
        return entityMapper.toDTO(quiz);
    }

    @Override
    public void deleteQuizByLessonId(Long lessonId /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for deleting quiz for lesson " + lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        Quiz quiz = quizRepository.findByLessonId(lessonId) // Assumes findByLessonId exists
                .orElseThrow(() -> new ResourceNotFoundException("Quiz for Lesson", "lessonId", lessonId));

        lesson.setQuiz(null); // Unlink first
        lessonRepository.save(lesson);
        quizRepository.delete(quiz); // Cascade handles questions
    }
}