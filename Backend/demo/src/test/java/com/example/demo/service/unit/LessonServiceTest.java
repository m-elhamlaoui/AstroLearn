package com.example.demo.service.unit;//package com.example.demo.service.unit;

import com.example.demo.dto.LessonDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LessonServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddLessonToModule_Success() {
        Long moduleId = 1L;
        LessonDTO lessonDTO = new LessonDTO(null, "Test Lesson", "Test Content", "http://video.url", null, null);

        Module module = new Module();
        module.setId(moduleId);

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDTO.title());
        lesson.setContent(lessonDTO.content());
        lesson.setVideoUrl(lessonDTO.videoUrl());

        Lesson savedLesson = new Lesson();
        savedLesson.setId(1L);
        savedLesson.setTitle(lessonDTO.title());
        savedLesson.setContent(lessonDTO.content());
        savedLesson.setVideoUrl(lessonDTO.videoUrl());
        savedLesson.setModule(module);

        LessonDTO expectedDTO = new LessonDTO(1L, "Test Lesson", "Test Content", "http://video.url", moduleId, null);

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(entityMapper.toEntity(lessonDTO)).thenReturn(lesson);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);
        when(entityMapper.toDTO(savedLesson)).thenReturn(expectedDTO);

        LessonDTO result = lessonService.addLessonToModule(moduleId, lessonDTO);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.content(), result.content());
        assertEquals(expectedDTO.videoUrl(), result.videoUrl());
        verify(moduleRepository, times(1)).findById(moduleId);
        verify(entityMapper, times(1)).toEntity(lessonDTO);
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(entityMapper, times(1)).toDTO(savedLesson);
    }

    @Test
    void testAddLessonToModule_ModuleNotFound() {
        Long moduleId = 1L;
        LessonDTO lessonDTO = new LessonDTO(null, "Test Lesson", "Test Content", "http://video.url", null, null);

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lessonService.addLessonToModule(moduleId, lessonDTO));
        verify(moduleRepository, times(1)).findById(moduleId);
        verifyNoMoreInteractions(moduleRepository, lessonRepository, entityMapper);
    }

    @Test
    void testGetLessonById_Success() {
        Long lessonId = 1L;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Test Lesson");
        lesson.setContent("Test Content");
        lesson.setVideoUrl("http://video.url");

        LessonDTO expectedDTO = new LessonDTO(lessonId, "Test Lesson", "Test Content", "http://video.url", 1L, null);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(entityMapper.toDTO(lesson)).thenReturn(expectedDTO);

        LessonDTO result = lessonService.getLessonById(lessonId);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.content(), result.content());
        assertEquals(expectedDTO.videoUrl(), result.videoUrl());
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(entityMapper, times(1)).toDTO(lesson);
    }

    @Test
    void testGetLessonById_NotFound() {
        Long lessonId = 1L;
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonById(lessonId));
        verify(lessonRepository, times(1)).findById(lessonId);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void testUpdateLesson_Success() {
        Long lessonId = 1L;
        LessonDTO updatedDTO = new LessonDTO(lessonId, "Updated Lesson", "Updated Content", "http://updated.video.url", 1L, null);

        Lesson existingLesson = new Lesson();
        existingLesson.setId(lessonId);
        existingLesson.setTitle("Original Lesson");
        existingLesson.setContent("Original Content");
        existingLesson.setVideoUrl("http://original.video.url");

        Lesson updatedLesson = new Lesson();
        updatedLesson.setId(lessonId);
        updatedLesson.setTitle(updatedDTO.title());
        updatedLesson.setContent(updatedDTO.content());
        updatedLesson.setVideoUrl(updatedDTO.videoUrl());

        LessonDTO expectedDTO = new LessonDTO(lessonId, "Updated Lesson", "Updated Content", "http://updated.video.url", 1L, null);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(existingLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(updatedLesson);
        when(entityMapper.toDTO(updatedLesson)).thenReturn(expectedDTO);

        LessonDTO result = lessonService.updateLesson(lessonId, updatedDTO);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.content(), result.content());
        assertEquals(expectedDTO.videoUrl(), result.videoUrl());
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(entityMapper, times(1)).toDTO(updatedLesson);
    }

    @Test
    void testUpdateLesson_NotFound() {
        Long lessonId = 1L;
        LessonDTO updatedDTO = new LessonDTO(lessonId, "Updated Lesson", "Updated Content", "http://updated.video.url", 1L, null);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lessonService.updateLesson(lessonId, updatedDTO));
        verify(lessonRepository, times(1)).findById(lessonId);
        verifyNoMoreInteractions(lessonRepository, entityMapper);
    }

    @Test
    void testDeleteLesson_Success() {
        Long lessonId = 1L;
        when(lessonRepository.existsById(lessonId)).thenReturn(true);
        doNothing().when(lessonRepository).deleteById(lessonId);

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository, times(1)).existsById(lessonId);
        verify(lessonRepository, times(1)).deleteById(lessonId);
    }

    @Test
    void testDeleteLesson_NotFound() {
        Long lessonId = 1L;
        when(lessonRepository.existsById(lessonId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> lessonService.deleteLesson(lessonId));
        verify(lessonRepository, times(1)).existsById(lessonId);
        verifyNoMoreInteractions(lessonRepository);
    }

    @Test
    void testGetLessonsByModuleId_Success() {
        Long moduleId = 1L;
        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setTitle("Lesson 1");
        lesson1.setContent("Content 1");
        lesson1.setVideoUrl("http://video1.url");

        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setTitle("Lesson 2");
        lesson2.setContent("Content 2");
        lesson2.setVideoUrl("http://video2.url");

        List<Lesson> lessons = List.of(lesson1, lesson2);

        LessonDTO dto1 = new LessonDTO(1L, "Lesson 1", "Content 1", "http://video1.url", moduleId, null);
        LessonDTO dto2 = new LessonDTO(2L, "Lesson 2", "Content 2", "http://video2.url", moduleId, null);

        when(moduleRepository.existsById(moduleId)).thenReturn(true);
        when(lessonRepository.findByModuleId(moduleId)).thenReturn(lessons);
        when(entityMapper.toDTO(lesson1)).thenReturn(dto1);
        when(entityMapper.toDTO(lesson2)).thenReturn(dto2);

        List<LessonDTO> results = lessonService.getLessonsByModuleId(moduleId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(dto1.id(), results.get(0).id());
        assertEquals(dto2.id(), results.get(1).id());
        verify(moduleRepository, times(1)).existsById(moduleId);
        verify(lessonRepository, times(1)).findByModuleId(moduleId);
        verify(entityMapper, times(1)).toDTO(lesson1);
        verify(entityMapper, times(1)).toDTO(lesson2);
    }

    @Test
    void testGetLessonsByModuleId_ModuleNotFound() {
        Long moduleId = 1L;
        when(moduleRepository.existsById(moduleId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonsByModuleId(moduleId));
        verify(moduleRepository, times(1)).existsById(moduleId);
        verifyNoMoreInteractions(moduleRepository, lessonRepository, entityMapper);
    }
}