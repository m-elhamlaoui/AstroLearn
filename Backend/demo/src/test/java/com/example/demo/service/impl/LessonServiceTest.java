package com.example.demo.service.impl;

import com.example.demo.dto.LessonDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.LessonMapper;
import com.example.demo.model.Lesson;
import com.example.demo.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Lesson lesson;
    private LessonDTO lessonDTO;
    private Long lessonId = 1L;
    private Long nonExistentLessonId = 999L;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Test Lesson");
        lesson.setContent("Test Content");

        lessonDTO = new LessonDTO();
        lessonDTO.setId(lessonId);
        lessonDTO.setTitle("Test Lesson");
        lessonDTO.setContent("Test Content");
    }

    @Test
    void getLessonById_WhenLessonExists_ShouldReturnLessonDTO() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toDto(lesson)).thenReturn(lessonDTO);

        LessonDTO result = lessonService.getLessonById(lessonId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(lessonId);
        assertThat(result.getTitle()).isEqualTo(lessonDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(lessonDTO.getContent());

        verify(lessonRepository).findById(lessonId);
        verify(lessonMapper).toDto(lesson);
    }

    @Test
    void getLessonById_WhenLessonDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(lessonRepository.findById(nonExistentLessonId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.getLessonById(nonExistentLessonId);
        });

        assertThat(exception.getMessage()).contains("Lesson not found with id: " + nonExistentLessonId);

        verify(lessonRepository).findById(nonExistentLessonId);
        verify(lessonMapper, never()).toDto(any(Lesson.class));
    }

    @Test
    void getAllLessons_ShouldReturnListOfLessonDTOs() {
        List<Lesson> lessons = Arrays.asList(lesson);
        List<LessonDTO> lessonDTOs = Arrays.asList(lessonDTO);

        when(lessonRepository.findAll()).thenReturn(lessons);
        when(lessonMapper.toDto(lesson)).thenReturn(lessonDTO);

        List<LessonDTO> result = lessonService.getAllLessons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(lessonId);
        assertThat(result.get(0).getTitle()).isEqualTo(lessonDTO.getTitle());
        assertThat(result.get(0).getContent()).isEqualTo(lessonDTO.getContent());

        verify(lessonRepository).findAll();
        verify(lessonMapper).toDto(lesson);
    }

    @Test
    void createLesson_ShouldReturnCreatedLessonDTO() {
        when(lessonMapper.toEntity(lessonDTO)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDto(lesson)).thenReturn(lessonDTO);

        LessonDTO result = lessonService.createLesson(lessonDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(lessonId);
        assertThat(result.getTitle()).isEqualTo(lessonDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(lessonDTO.getContent());

        verify(lessonMapper).toEntity(lessonDTO);
        verify(lessonRepository).save(lesson);
        verify(lessonMapper).toDto(lesson);
    }

    @Test
    void updateLesson_WhenLessonExists_ShouldReturnUpdatedLessonDTO() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toEntity(lessonDTO)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonMapper.toDto(lesson)).thenReturn(lessonDTO);

        LessonDTO result = lessonService.updateLesson(lessonId, lessonDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(lessonId);
        assertThat(result.getTitle()).isEqualTo(lessonDTO.getTitle());
        assertThat(result.getContent()).isEqualTo(lessonDTO.getContent());

        verify(lessonRepository).findById(lessonId);
        verify(lessonMapper).toEntity(lessonDTO);
        verify(lessonRepository).save(lesson);
        verify(lessonMapper).toDto(lesson);
    }

    @Test
    void updateLesson_WhenLessonDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(lessonRepository.findById(nonExistentLessonId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.updateLesson(nonExistentLessonId, lessonDTO);
        });

        assertThat(exception.getMessage()).contains("Lesson not found with id: " + nonExistentLessonId);

        verify(lessonRepository).findById(nonExistentLessonId);
        verify(lessonMapper, never()).toEntity(any(LessonDTO.class));
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(lessonMapper, never()).toDto(any(Lesson.class));
    }

    @Test
    void deleteLesson_WhenLessonExists_ShouldDeleteSuccessfully() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        doNothing().when(lessonRepository).deleteById(lessonId);

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository).findById(lessonId);
        verify(lessonRepository).deleteById(lessonId);
    }

    @Test
    void deleteLesson_WhenLessonDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(lessonRepository.findById(nonExistentLessonId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.deleteLesson(nonExistentLessonId);
        });

        assertThat(exception.getMessage()).contains("Lesson not found with id: " + nonExistentLessonId);

        verify(lessonRepository).findById(nonExistentLessonId);
        verify(lessonRepository, never()).deleteById(anyLong());
    }
}
