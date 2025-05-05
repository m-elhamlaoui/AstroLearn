package com.example.demo.service.impl;

import com.example.demo.dto.CourseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private CourseDTO courseDTO;
    private Long courseId = 1L;
    private Long nonExistentCourseId = 999L;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(courseId);
        course.setTitle("Test Course");
        course.setDescription("Test Description");

        courseDTO = new CourseDTO();
        courseDTO.setId(courseId);
        courseDTO.setTitle("Test Course");
        courseDTO.setDescription("Test Description");
    }

    @Test
    void getCourseById_WhenCourseExists_ShouldReturnCourseDTO() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toDto(course)).thenReturn(courseDTO);

        CourseDTO result = courseService.getCourseById(courseId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseId);
        assertThat(result.getTitle()).isEqualTo(courseDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(courseDTO.getDescription());

        verify(courseRepository).findById(courseId);
        verify(courseMapper).toDto(course);
    }

    @Test
    void getCourseById_WhenCourseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(nonExistentCourseId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseService.getCourseById(nonExistentCourseId);
        });

        assertThat(exception.getMessage()).contains("Course not found with id: " + nonExistentCourseId);

        verify(courseRepository).findById(nonExistentCourseId);
        verify(courseMapper, never()).toDto(any(Course.class));
    }

    @Test
    void getAllCourses_ShouldReturnListOfCourseDTOs() {
        List<Course> courses = Arrays.asList(course);
        List<CourseDTO> courseDTOs = Arrays.asList(courseDTO);

        when(courseRepository.findAll()).thenReturn(courses);
        when(courseMapper.toDto(course)).thenReturn(courseDTO);

        List<CourseDTO> result = courseService.getAllCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(courseId);
        assertThat(result.get(0).getTitle()).isEqualTo(courseDTO.getTitle());
        assertThat(result.get(0).getDescription()).isEqualTo(courseDTO.getDescription());

        verify(courseRepository).findAll();
        verify(courseMapper).toDto(course);
    }

    @Test
    void createCourse_ShouldReturnCreatedCourseDTO() {
        when(courseMapper.toEntity(courseDTO)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toDto(course)).thenReturn(courseDTO);

        CourseDTO result = courseService.createCourse(courseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseId);
        assertThat(result.getTitle()).isEqualTo(courseDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(courseDTO.getDescription());

        verify(courseMapper).toEntity(courseDTO);
        verify(courseRepository).save(course);
        verify(courseMapper).toDto(course);
    }

    @Test
    void updateCourse_WhenCourseExists_ShouldReturnUpdatedCourseDTO() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toEntity(courseDTO)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toDto(course)).thenReturn(courseDTO);

        CourseDTO result = courseService.updateCourse(courseId, courseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseId);
        assertThat(result.getTitle()).isEqualTo(courseDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(courseDTO.getDescription());

        verify(courseRepository).findById(courseId);
        verify(courseMapper).toEntity(courseDTO);
        verify(courseRepository).save(course);
        verify(courseMapper).toDto(course);
    }

    @Test
    void updateCourse_WhenCourseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(nonExistentCourseId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseService.updateCourse(nonExistentCourseId, courseDTO);
        });

        assertThat(exception.getMessage()).contains("Course not found with id: " + nonExistentCourseId);

        verify(courseRepository).findById(nonExistentCourseId);
        verify(courseMapper, never()).toEntity(any(CourseDTO.class));
        verify(courseRepository, never()).save(any(Course.class));
        verify(courseMapper, never()).toDto(any(Course.class));
    }

    @Test
    void deleteCourse_WhenCourseExists_ShouldDeleteSuccessfully() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).deleteById(courseId);

        courseService.deleteCourse(courseId);

        verify(courseRepository).findById(courseId);
        verify(courseRepository).deleteById(courseId);
    }

    @Test
    void deleteCourse_WhenCourseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseRepository.findById(nonExistentCourseId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseService.deleteCourse(nonExistentCourseId);
        });

        assertThat(exception.getMessage()).contains("Course not found with id: " + nonExistentCourseId);

        verify(courseRepository).findById(nonExistentCourseId);
        verify(courseRepository, never()).deleteById(anyLong());
    }
}
