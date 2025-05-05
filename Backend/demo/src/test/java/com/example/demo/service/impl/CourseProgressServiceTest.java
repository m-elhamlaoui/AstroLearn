package com.example.demo.service.impl;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseProgressMapper;
import com.example.demo.model.CourseProgress;
import com.example.demo.repository.CourseProgressRepository;
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
class CourseProgressServiceTest {

    @Mock
    private CourseProgressRepository courseProgressRepository;

    @Mock
    private CourseProgressMapper courseProgressMapper;

    @InjectMocks
    private CourseProgressServiceImpl courseProgressService;

    private CourseProgress courseProgress;
    private CourseProgressDTO courseProgressDTO;
    private Long courseProgressId = 1L;
    private Long nonExistentCourseProgressId = 999L;

    @BeforeEach
    void setUp() {
        courseProgress = new CourseProgress();
        courseProgress.setId(courseProgressId);
        courseProgress.setUserId(1L);
        courseProgress.setCourseId(1L);
        courseProgress.setProgress(50);

        courseProgressDTO = new CourseProgressDTO();
        courseProgressDTO.setId(courseProgressId);
        courseProgressDTO.setUserId(1L);
        courseProgressDTO.setCourseId(1L);
        courseProgressDTO.setProgress(50);
    }

    @Test
    void getCourseProgressById_WhenCourseProgressExists_ShouldReturnCourseProgressDTO() {
        when(courseProgressRepository.findById(courseProgressId)).thenReturn(Optional.of(courseProgress));
        when(courseProgressMapper.toDto(courseProgress)).thenReturn(courseProgressDTO);

        CourseProgressDTO result = courseProgressService.getCourseProgressById(courseProgressId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseProgressId);
        assertThat(result.getUserId()).isEqualTo(courseProgressDTO.getUserId());
        assertThat(result.getCourseId()).isEqualTo(courseProgressDTO.getCourseId());
        assertThat(result.getProgress()).isEqualTo(courseProgressDTO.getProgress());

        verify(courseProgressRepository).findById(courseProgressId);
        verify(courseProgressMapper).toDto(courseProgress);
    }

    @Test
    void getCourseProgressById_WhenCourseProgressDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseProgressRepository.findById(nonExistentCourseProgressId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseProgressService.getCourseProgressById(nonExistentCourseProgressId);
        });

        assertThat(exception.getMessage()).contains("CourseProgress not found with id: " + nonExistentCourseProgressId);

        verify(courseProgressRepository).findById(nonExistentCourseProgressId);
        verify(courseProgressMapper, never()).toDto(any(CourseProgress.class));
    }

    @Test
    void getAllCourseProgresses_ShouldReturnListOfCourseProgressDTOs() {
        List<CourseProgress> courseProgresses = Arrays.asList(courseProgress);
        List<CourseProgressDTO> courseProgressDTOs = Arrays.asList(courseProgressDTO);

        when(courseProgressRepository.findAll()).thenReturn(courseProgresses);
        when(courseProgressMapper.toDto(courseProgress)).thenReturn(courseProgressDTO);

        List<CourseProgressDTO> result = courseProgressService.getAllCourseProgresses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(courseProgressId);
        assertThat(result.get(0).getUserId()).isEqualTo(courseProgressDTO.getUserId());
        assertThat(result.get(0).getCourseId()).isEqualTo(courseProgressDTO.getCourseId());
        assertThat(result.get(0).getProgress()).isEqualTo(courseProgressDTO.getProgress());

        verify(courseProgressRepository).findAll();
        verify(courseProgressMapper).toDto(courseProgress);
    }

    @Test
    void createCourseProgress_ShouldReturnCreatedCourseProgressDTO() {
        when(courseProgressMapper.toEntity(courseProgressDTO)).thenReturn(courseProgress);
        when(courseProgressRepository.save(courseProgress)).thenReturn(courseProgress);
        when(courseProgressMapper.toDto(courseProgress)).thenReturn(courseProgressDTO);

        CourseProgressDTO result = courseProgressService.createCourseProgress(courseProgressDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseProgressId);
        assertThat(result.getUserId()).isEqualTo(courseProgressDTO.getUserId());
        assertThat(result.getCourseId()).isEqualTo(courseProgressDTO.getCourseId());
        assertThat(result.getProgress()).isEqualTo(courseProgressDTO.getProgress());

        verify(courseProgressMapper).toEntity(courseProgressDTO);
        verify(courseProgressRepository).save(courseProgress);
        verify(courseProgressMapper).toDto(courseProgress);
    }

    @Test
    void updateCourseProgress_WhenCourseProgressExists_ShouldReturnUpdatedCourseProgressDTO() {
        when(courseProgressRepository.findById(courseProgressId)).thenReturn(Optional.of(courseProgress));
        when(courseProgressMapper.toEntity(courseProgressDTO)).thenReturn(courseProgress);
        when(courseProgressRepository.save(courseProgress)).thenReturn(courseProgress);
        when(courseProgressMapper.toDto(courseProgress)).thenReturn(courseProgressDTO);

        CourseProgressDTO result = courseProgressService.updateCourseProgress(courseProgressId, courseProgressDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseProgressId);
        assertThat(result.getUserId()).isEqualTo(courseProgressDTO.getUserId());
        assertThat(result.getCourseId()).isEqualTo(courseProgressDTO.getCourseId());
        assertThat(result.getProgress()).isEqualTo(courseProgressDTO.getProgress());

        verify(courseProgressRepository).findById(courseProgressId);
        verify(courseProgressMapper).toEntity(courseProgressDTO);
        verify(courseProgressRepository).save(courseProgress);
        verify(courseProgressMapper).toDto(courseProgress);
    }

    @Test
    void updateCourseProgress_WhenCourseProgressDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseProgressRepository.findById(nonExistentCourseProgressId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseProgressService.updateCourseProgress(nonExistentCourseProgressId, courseProgressDTO);
        });

        assertThat(exception.getMessage()).contains("CourseProgress not found with id: " + nonExistentCourseProgressId);

        verify(courseProgressRepository).findById(nonExistentCourseProgressId);
        verify(courseProgressMapper, never()).toEntity(any(CourseProgressDTO.class));
        verify(courseProgressRepository, never()).save(any(CourseProgress.class));
        verify(courseProgressMapper, never()).toDto(any(CourseProgress.class));
    }

    @Test
    void deleteCourseProgress_WhenCourseProgressExists_ShouldDeleteSuccessfully() {
        when(courseProgressRepository.findById(courseProgressId)).thenReturn(Optional.of(courseProgress));
        doNothing().when(courseProgressRepository).deleteById(courseProgressId);

        courseProgressService.deleteCourseProgress(courseProgressId);

        verify(courseProgressRepository).findById(courseProgressId);
        verify(courseProgressRepository).deleteById(courseProgressId);
    }

    @Test
    void deleteCourseProgress_WhenCourseProgressDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(courseProgressRepository.findById(nonExistentCourseProgressId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseProgressService.deleteCourseProgress(nonExistentCourseProgressId);
        });

        assertThat(exception.getMessage()).contains("CourseProgress not found with id: " + nonExistentCourseProgressId);

        verify(courseProgressRepository).findById(nonExistentCourseProgressId);
        verify(courseProgressRepository, never()).deleteById(anyLong());
    }
}
