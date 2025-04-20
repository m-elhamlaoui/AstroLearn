package com.example.demo.service.impl;

import com.example.demo.dto.LessonDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EntityMapper entityMapper;

    @Override
    public LessonDTO addLessonToModule(Long moduleId, LessonDTO lessonDTO) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        Lesson lesson = entityMapper.toEntity(lessonDTO);
        lesson.setModule(module);
        lesson.setQuiz(null);
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
    public LessonDTO updateLesson(Long lessonId, LessonDTO lessonDTO) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        existingLesson.setTitle(lessonDTO.title());
        existingLesson.setContent(lessonDTO.content());
        existingLesson.setVideoUrl(lessonDTO.videoUrl());
        Lesson updatedLesson = lessonRepository.save(existingLesson);
        return entityMapper.toDTO(updatedLesson);
    }

    @Override
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByModuleId(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }
        return lessonRepository.findByModuleId(moduleId).stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}