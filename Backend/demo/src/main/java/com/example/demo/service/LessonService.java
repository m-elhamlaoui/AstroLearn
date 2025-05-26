package com.example.demo.service;

import com.example.demo.dto.LessonDTO;

import java.util.List;

public interface LessonService {
    LessonDTO addLessonToModule(Long moduleId, LessonDTO lessonDTO);
    LessonDTO getLessonById(Long lessonId);
    LessonDTO updateLesson(Long lessonId, LessonDTO lessonDTO);
    void deleteLesson(Long lessonId);
    List<LessonDTO> getLessonsByModuleId(Long moduleId);
    List<LessonDTO> reorderLessons(Long moduleId, List<Long> lessonIds);
}