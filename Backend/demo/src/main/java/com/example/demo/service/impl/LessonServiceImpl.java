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
import java.util.Map;
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
                .sorted((l1, l2) -> Integer.compare(l1.getOrderIndex(), l2.getOrderIndex()))
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public List<LessonDTO> reorderLessons(Long moduleId, List<Long> lessonIds) {
        // Verify module exists
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }
        
        // Fetch all lessons for this module
        List<Lesson> lessons = lessonRepository.findByModuleId(moduleId);
        
        // Create a map of lesson ID to lesson for quick lookup
        Map<Long, Lesson> lessonMap = lessons.stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));
        
        // Update the order of each lesson based on its position in the lessonIds list
        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessonMap.get(lessonId);
            
            if (lesson != null) {
                lesson.setOrderIndex(i);
                lessonRepository.save(lesson);
            }
        }
        
        // Return the updated lessons
        return lessonRepository.findByModuleId(moduleId).stream()
                .sorted((l1, l2) -> Integer.compare(l1.getOrderIndex(), l2.getOrderIndex()))
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}