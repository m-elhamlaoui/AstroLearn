package com.example.demo.service.unit;

import com.example.demo.dto.CourseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.service.impl.CourseServiceImpl;
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

class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourse_Success() {
        // Arrange
        CourseDTO courseDTO = new CourseDTO(null, "Test Course", "Test Description", null, Course.DifficultyLevel.BEGINNER, 0, Collections.emptyList());
        Course course = new Course();
        course.setTitle(courseDTO.title());
        course.setDescription(courseDTO.description());
        course.setDifficulty(courseDTO.difficulty());
        course.setModules(Collections.emptyList());
        course.setProgresses(Collections.emptyList());

        Course savedCourse = new Course();
        savedCourse.setId(1L);
        savedCourse.setTitle(courseDTO.title());
        savedCourse.setDescription(courseDTO.description());
        savedCourse.setDifficulty(courseDTO.difficulty());
        savedCourse.setModules(Collections.emptyList());
        savedCourse.setProgresses(Collections.emptyList());

        CourseDTO expectedDTO = new CourseDTO(1L, "Test Course", "Test Description", null, Course.DifficultyLevel.BEGINNER, 0, Collections.emptyList());

        when(entityMapper.toEntity(courseDTO)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);
        when(entityMapper.toDTO(savedCourse)).thenReturn(expectedDTO);

        // Act
        CourseDTO result = courseService.createCourse(courseDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        verify(entityMapper, times(1)).toEntity(courseDTO);
        verify(courseRepository, times(1)).save(any(Course.class));
        verify(entityMapper, times(1)).toDTO(savedCourse);
    }

    @Test
    void testGetCourseById_Success() {
        // Arrange
        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Test Course");
        course.setDescription("Test Description");
        course.setDifficulty(Course.DifficultyLevel.BEGINNER);
        course.setModules(Collections.emptyList());
        course.setProgresses(Collections.emptyList());

        CourseDTO expectedDTO = new CourseDTO(courseId, "Test Course", "Test Description", null, Course.DifficultyLevel.BEGINNER, 0, Collections.emptyList());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(entityMapper.toDTO(course)).thenReturn(expectedDTO);

        // Act
        CourseDTO result = courseService.getCourseById(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        verify(courseRepository, times(1)).findById(courseId);
        verify(entityMapper, times(1)).toDTO(course);
    }

    @Test
    void testGetCourseById_NotFound() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(courseId));
        verify(courseRepository, times(1)).findById(courseId);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void testGetAllCourses_Success() {
        // Arrange
        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Course 1");
        course1.setDescription("Description 1");
        course1.setDifficulty(Course.DifficultyLevel.BEGINNER);
        course1.setModules(Collections.emptyList());
        course1.setProgresses(Collections.emptyList());

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Course 2");
        course2.setDescription("Description 2");
        course2.setDifficulty(Course.DifficultyLevel.INTERMEDIATE);
        course2.setModules(Collections.emptyList());
        course2.setProgresses(Collections.emptyList());

        List<Course> courses = List.of(course1, course2);

        CourseDTO dto1 = new CourseDTO(1L, "Course 1", "Description 1", null, Course.DifficultyLevel.BEGINNER, 0, Collections.emptyList());
        CourseDTO dto2 = new CourseDTO(2L, "Course 2", "Description 2", null, Course.DifficultyLevel.INTERMEDIATE, 0, Collections.emptyList());

        when(courseRepository.findAll()).thenReturn(courses);
        when(entityMapper.toDTO(course1)).thenReturn(dto1);
        when(entityMapper.toDTO(course2)).thenReturn(dto2);

        // Act
        List<CourseDTO> results = courseService.getAllCourses();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(dto1.id(), results.get(0).id());
        assertEquals(dto2.id(), results.get(1).id());
        verify(courseRepository, times(1)).findAll();
        verify(entityMapper, times(1)).toDTO(course1);
        verify(entityMapper, times(1)).toDTO(course2);
    }

    @Test
    void testUpdateCourse_Success() {
        // Arrange
        Long courseId = 1L;
        CourseDTO updatedDTO = new CourseDTO(courseId, "Updated Course", "Updated Description", null, Course.DifficultyLevel.INTERMEDIATE, 0, Collections.emptyList());

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Original Course");
        existingCourse.setDescription("Original Description");
        existingCourse.setDifficulty(Course.DifficultyLevel.BEGINNER);
        existingCourse.setModules(Collections.emptyList());
        existingCourse.setProgresses(Collections.emptyList());

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle(updatedDTO.title());
        updatedCourse.setDescription(updatedDTO.description());
        updatedCourse.setDifficulty(updatedDTO.difficulty());
        updatedCourse.setModules(Collections.emptyList());
        updatedCourse.setProgresses(Collections.emptyList());

        CourseDTO expectedDTO = new CourseDTO(courseId, "Updated Course", "Updated Description", null, Course.DifficultyLevel.INTERMEDIATE, 0, Collections.emptyList());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);
        when(entityMapper.toDTO(updatedCourse)).thenReturn(expectedDTO);

        // Act
        CourseDTO result = courseService.updateCourse(courseId, updatedDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.description(), result.description());
        assertEquals(expectedDTO.difficulty(), result.difficulty());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).save(any(Course.class));
        verify(entityMapper, times(1)).toDTO(updatedCourse);
    }

    @Test
    void testUpdateCourse_NotFound() {
        // Arrange
        Long courseId = 1L;
        CourseDTO updatedDTO = new CourseDTO(courseId, "Updated Course", "Updated Description", null, Course.DifficultyLevel.INTERMEDIATE, 0, Collections.emptyList());

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(courseId, updatedDTO));
        verify(courseRepository, times(1)).findById(courseId);
        verifyNoMoreInteractions(courseRepository, entityMapper);
    }

    @Test
    void testDeleteCourse_Success() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.existsById(courseId)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(courseId);

        // Act
        courseService.deleteCourse(courseId);

        // Assert
        verify(courseRepository, times(1)).existsById(courseId);
        verify(courseRepository, times(1)).deleteById(courseId);
    }

    @Test
    void testDeleteCourse_NotFound() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.existsById(courseId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(courseId));
        verify(courseRepository, times(1)).existsById(courseId);
        verifyNoMoreInteractions(courseRepository);
    }
}