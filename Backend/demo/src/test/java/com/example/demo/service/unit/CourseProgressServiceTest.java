package com.example.demo.service.unit;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.model.Module;
import com.example.demo.repository.*;
import com.example.demo.service.impl.CourseProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseProgressServiceTest {

    @Mock
    private CourseProgressRepository courseProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private Course course; // Mock the Course object

    @InjectMocks
    private CourseProgressServiceImpl courseProgressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOrCreateCourseProgress_NewProgress() {
        Long userId = 1L;
        Long courseId = 1L;

        User user = new User();
        user.setId(userId);

        when(course.getTotalLessons()).thenReturn(10); // Mock the getTotalLessons method
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseProgressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        CourseProgress progress = new CourseProgress();
        progress.setUser(user);
        progress.setCourse(course);
        progress.setCompleted(false);
        progress.setCompletionPercentage(0.0);
        progress.setLastAccessed(LocalDateTime.now());

        CourseProgress savedProgress = new CourseProgress();
        savedProgress.setId(1L);
        savedProgress.setUser(user);
        savedProgress.setCourse(course);
        savedProgress.setCompleted(false);
        savedProgress.setCompletionPercentage(0.0);
        savedProgress.setLastAccessed(LocalDateTime.now());

        CourseProgressDTO expectedDTO = new CourseProgressDTO(
                1L, 0.0, false, savedProgress.getLastAccessed(), userId, courseId, null, Set.of()
        );

        when(courseProgressRepository.save(any(CourseProgress.class))).thenReturn(savedProgress);
        when(entityMapper.toDTO(savedProgress)).thenReturn(expectedDTO);

        CourseProgressDTO result = courseProgressService.getOrCreateCourseProgress(userId, courseId);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.completionPercentage(), result.completionPercentage());
        verify(userRepository, times(1)).findById(userId);
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseProgressRepository, times(1)).findByUserIdAndCourseId(userId, courseId);
        verify(courseProgressRepository, times(1)).save(any(CourseProgress.class));
        verify(entityMapper, times(1)).toDTO(savedProgress);
    }

    @Test
    void testMarkLessonCompleted_Success() {
        Long userId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        // Mock the course and its total lessons
        when(course.getId()).thenReturn(courseId);
        when(course.getTotalLessons()).thenReturn(10);

        // Create and associate the lesson with the course
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        Module module = new Module();
        module.setCourse(course); // Associate the module with the mocked course
        lesson.setModule(module);

        // Mock the course progress
        CourseProgress progress = new CourseProgress();
        progress.setId(1L);
        progress.setCourse(course);
        progress.setCompletedLessonIds(Set.of());

        // Mock the updated progress
        CourseProgress updatedProgress = new CourseProgress();
        updatedProgress.setId(1L);
        updatedProgress.setCourse(course);
        updatedProgress.setCompletedLessonIds(Set.of(lessonId));
        updatedProgress.setCompletionPercentage(10.0);

        // Expected DTO
        CourseProgressDTO expectedDTO = new CourseProgressDTO(
                1L, 10.0, false, LocalDateTime.now(), userId, courseId, null, Set.of(lessonId)
        );

        // Mock repository and mapper behavior
        when(courseProgressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.of(progress));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseProgressRepository.save(any(CourseProgress.class))).thenReturn(updatedProgress);
        when(entityMapper.toDTO(updatedProgress)).thenReturn(expectedDTO);

        // Act
        CourseProgressDTO result = courseProgressService.markLessonCompleted(userId, courseId, lessonId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.completionPercentage(), result.completionPercentage());
        assertTrue(result.completedLessonIds().contains(lessonId));
        verify(courseProgressRepository, times(1)).findByUserIdAndCourseId(userId, courseId);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(courseProgressRepository, times(1)).save(any(CourseProgress.class));
        verify(entityMapper, times(1)).toDTO(updatedProgress);
    }


}