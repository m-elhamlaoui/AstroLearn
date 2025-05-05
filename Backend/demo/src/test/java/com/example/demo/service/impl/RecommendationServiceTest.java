package com.example.demo.service.impl;

import com.example.demo.dto.RecommendationDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.RecommendationMapper;
import com.example.demo.model.Recommendation;
import com.example.demo.repository.RecommendationRepository;
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
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Recommendation recommendation;
    private RecommendationDTO recommendationDTO;
    private Long recommendationId = 1L;
    private Long nonExistentRecommendationId = 999L;

    @BeforeEach
    void setUp() {
        recommendation = new Recommendation();
        recommendation.setId(recommendationId);
        recommendation.setUserId(1L);
        recommendation.setArticleId(1L);
        recommendation.setScore(0.8);

        recommendationDTO = new RecommendationDTO();
        recommendationDTO.setId(recommendationId);
        recommendationDTO.setUserId(1L);
        recommendationDTO.setArticleId(1L);
        recommendationDTO.setScore(0.8);
    }

    @Test
    void getRecommendationById_WhenRecommendationExists_ShouldReturnRecommendationDTO() {
        when(recommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDTO);

        RecommendationDTO result = recommendationService.getRecommendationById(recommendationId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(recommendationId);
        assertThat(result.getUserId()).isEqualTo(recommendationDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(recommendationDTO.getArticleId());
        assertThat(result.getScore()).isEqualTo(recommendationDTO.getScore());

        verify(recommendationRepository).findById(recommendationId);
        verify(recommendationMapper).toDto(recommendation);
    }

    @Test
    void getRecommendationById_WhenRecommendationDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(recommendationRepository.findById(nonExistentRecommendationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            recommendationService.getRecommendationById(nonExistentRecommendationId);
        });

        assertThat(exception.getMessage()).contains("Recommendation not found with id: " + nonExistentRecommendationId);

        verify(recommendationRepository).findById(nonExistentRecommendationId);
        verify(recommendationMapper, never()).toDto(any(Recommendation.class));
    }

    @Test
    void getAllRecommendations_ShouldReturnListOfRecommendationDTOs() {
        List<Recommendation> recommendations = Arrays.asList(recommendation);
        List<RecommendationDTO> recommendationDTOs = Arrays.asList(recommendationDTO);

        when(recommendationRepository.findAll()).thenReturn(recommendations);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDTO);

        List<RecommendationDTO> result = recommendationService.getAllRecommendations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(recommendationId);
        assertThat(result.get(0).getUserId()).isEqualTo(recommendationDTO.getUserId());
        assertThat(result.get(0).getArticleId()).isEqualTo(recommendationDTO.getArticleId());
        assertThat(result.get(0).getScore()).isEqualTo(recommendationDTO.getScore());

        verify(recommendationRepository).findAll();
        verify(recommendationMapper).toDto(recommendation);
    }

    @Test
    void createRecommendation_ShouldReturnCreatedRecommendationDTO() {
        when(recommendationMapper.toEntity(recommendationDTO)).thenReturn(recommendation);
        when(recommendationRepository.save(recommendation)).thenReturn(recommendation);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDTO);

        RecommendationDTO result = recommendationService.createRecommendation(recommendationDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(recommendationId);
        assertThat(result.getUserId()).isEqualTo(recommendationDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(recommendationDTO.getArticleId());
        assertThat(result.getScore()).isEqualTo(recommendationDTO.getScore());

        verify(recommendationMapper).toEntity(recommendationDTO);
        verify(recommendationRepository).save(recommendation);
        verify(recommendationMapper).toDto(recommendation);
    }

    @Test
    void updateRecommendation_WhenRecommendationExists_ShouldReturnUpdatedRecommendationDTO() {
        when(recommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));
        when(recommendationMapper.toEntity(recommendationDTO)).thenReturn(recommendation);
        when(recommendationRepository.save(recommendation)).thenReturn(recommendation);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDTO);

        RecommendationDTO result = recommendationService.updateRecommendation(recommendationId, recommendationDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(recommendationId);
        assertThat(result.getUserId()).isEqualTo(recommendationDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(recommendationDTO.getArticleId());
        assertThat(result.getScore()).isEqualTo(recommendationDTO.getScore());

        verify(recommendationRepository).findById(recommendationId);
        verify(recommendationMapper).toEntity(recommendationDTO);
        verify(recommendationRepository).save(recommendation);
        verify(recommendationMapper).toDto(recommendation);
    }

    @Test
    void updateRecommendation_WhenRecommendationDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(recommendationRepository.findById(nonExistentRecommendationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            recommendationService.updateRecommendation(nonExistentRecommendationId, recommendationDTO);
        });

        assertThat(exception.getMessage()).contains("Recommendation not found with id: " + nonExistentRecommendationId);

        verify(recommendationRepository).findById(nonExistentRecommendationId);
        verify(recommendationMapper, never()).toEntity(any(RecommendationDTO.class));
        verify(recommendationRepository, never()).save(any(Recommendation.class));
        verify(recommendationMapper, never()).toDto(any(Recommendation.class));
    }

    @Test
    void deleteRecommendation_WhenRecommendationExists_ShouldDeleteSuccessfully() {
        when(recommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));
        doNothing().when(recommendationRepository).deleteById(recommendationId);

        recommendationService.deleteRecommendation(recommendationId);

        verify(recommendationRepository).findById(recommendationId);
        verify(recommendationRepository).deleteById(recommendationId);
    }

    @Test
    void deleteRecommendation_WhenRecommendationDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(recommendationRepository.findById(nonExistentRecommendationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            recommendationService.deleteRecommendation(nonExistentRecommendationId);
        });

        assertThat(exception.getMessage()).contains("Recommendation not found with id: " + nonExistentRecommendationId);

        verify(recommendationRepository).findById(nonExistentRecommendationId);
        verify(recommendationRepository, never()).deleteById(anyLong());
    }
}
