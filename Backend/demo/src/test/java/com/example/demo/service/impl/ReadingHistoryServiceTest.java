package com.example.demo.service.impl;

import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ReadingHistoryMapper;
import com.example.demo.model.ReadingHistory;
import com.example.demo.repository.ReadingHistoryRepository;
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
class ReadingHistoryServiceTest {

    @Mock
    private ReadingHistoryRepository readingHistoryRepository;

    @Mock
    private ReadingHistoryMapper readingHistoryMapper;

    @InjectMocks
    private ReadingHistoryServiceImpl readingHistoryService;

    private ReadingHistory readingHistory;
    private ReadingHistoryDTO readingHistoryDTO;
    private Long readingHistoryId = 1L;
    private Long nonExistentReadingHistoryId = 999L;

    @BeforeEach
    void setUp() {
        readingHistory = new ReadingHistory();
        readingHistory.setId(readingHistoryId);
        readingHistory.setUserId(1L);
        readingHistory.setArticleId(1L);
        readingHistory.setReadAt(java.time.LocalDateTime.now());

        readingHistoryDTO = new ReadingHistoryDTO();
        readingHistoryDTO.setId(readingHistoryId);
        readingHistoryDTO.setUserId(1L);
        readingHistoryDTO.setArticleId(1L);
        readingHistoryDTO.setReadAt(java.time.LocalDateTime.now());
    }

    @Test
    void getReadingHistoryById_WhenReadingHistoryExists_ShouldReturnReadingHistoryDTO() {
        when(readingHistoryRepository.findById(readingHistoryId)).thenReturn(Optional.of(readingHistory));
        when(readingHistoryMapper.toDto(readingHistory)).thenReturn(readingHistoryDTO);

        ReadingHistoryDTO result = readingHistoryService.getReadingHistoryById(readingHistoryId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(readingHistoryId);
        assertThat(result.getUserId()).isEqualTo(readingHistoryDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(readingHistoryDTO.getArticleId());
        assertThat(result.getReadAt()).isEqualTo(readingHistoryDTO.getReadAt());

        verify(readingHistoryRepository).findById(readingHistoryId);
        verify(readingHistoryMapper).toDto(readingHistory);
    }

    @Test
    void getReadingHistoryById_WhenReadingHistoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(readingHistoryRepository.findById(nonExistentReadingHistoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            readingHistoryService.getReadingHistoryById(nonExistentReadingHistoryId);
        });

        assertThat(exception.getMessage()).contains("ReadingHistory not found with id: " + nonExistentReadingHistoryId);

        verify(readingHistoryRepository).findById(nonExistentReadingHistoryId);
        verify(readingHistoryMapper, never()).toDto(any(ReadingHistory.class));
    }

    @Test
    void getAllReadingHistories_ShouldReturnListOfReadingHistoryDTOs() {
        List<ReadingHistory> readingHistories = Arrays.asList(readingHistory);
        List<ReadingHistoryDTO> readingHistoryDTOs = Arrays.asList(readingHistoryDTO);

        when(readingHistoryRepository.findAll()).thenReturn(readingHistories);
        when(readingHistoryMapper.toDto(readingHistory)).thenReturn(readingHistoryDTO);

        List<ReadingHistoryDTO> result = readingHistoryService.getAllReadingHistories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(readingHistoryId);
        assertThat(result.get(0).getUserId()).isEqualTo(readingHistoryDTO.getUserId());
        assertThat(result.get(0).getArticleId()).isEqualTo(readingHistoryDTO.getArticleId());
        assertThat(result.get(0).getReadAt()).isEqualTo(readingHistoryDTO.getReadAt());

        verify(readingHistoryRepository).findAll();
        verify(readingHistoryMapper).toDto(readingHistory);
    }

    @Test
    void createReadingHistory_ShouldReturnCreatedReadingHistoryDTO() {
        when(readingHistoryMapper.toEntity(readingHistoryDTO)).thenReturn(readingHistory);
        when(readingHistoryRepository.save(readingHistory)).thenReturn(readingHistory);
        when(readingHistoryMapper.toDto(readingHistory)).thenReturn(readingHistoryDTO);

        ReadingHistoryDTO result = readingHistoryService.createReadingHistory(readingHistoryDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(readingHistoryId);
        assertThat(result.getUserId()).isEqualTo(readingHistoryDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(readingHistoryDTO.getArticleId());
        assertThat(result.getReadAt()).isEqualTo(readingHistoryDTO.getReadAt());

        verify(readingHistoryMapper).toEntity(readingHistoryDTO);
        verify(readingHistoryRepository).save(readingHistory);
        verify(readingHistoryMapper).toDto(readingHistory);
    }

    @Test
    void updateReadingHistory_WhenReadingHistoryExists_ShouldReturnUpdatedReadingHistoryDTO() {
        when(readingHistoryRepository.findById(readingHistoryId)).thenReturn(Optional.of(readingHistory));
        when(readingHistoryMapper.toEntity(readingHistoryDTO)).thenReturn(readingHistory);
        when(readingHistoryRepository.save(readingHistory)).thenReturn(readingHistory);
        when(readingHistoryMapper.toDto(readingHistory)).thenReturn(readingHistoryDTO);

        ReadingHistoryDTO result = readingHistoryService.updateReadingHistory(readingHistoryId, readingHistoryDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(readingHistoryId);
        assertThat(result.getUserId()).isEqualTo(readingHistoryDTO.getUserId());
        assertThat(result.getArticleId()).isEqualTo(readingHistoryDTO.getArticleId());
        assertThat(result.getReadAt()).isEqualTo(readingHistoryDTO.getReadAt());

        verify(readingHistoryRepository).findById(readingHistoryId);
        verify(readingHistoryMapper).toEntity(readingHistoryDTO);
        verify(readingHistoryRepository).save(readingHistory);
        verify(readingHistoryMapper).toDto(readingHistory);
    }

    @Test
    void updateReadingHistory_WhenReadingHistoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(readingHistoryRepository.findById(nonExistentReadingHistoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            readingHistoryService.updateReadingHistory(nonExistentReadingHistoryId, readingHistoryDTO);
        });

        assertThat(exception.getMessage()).contains("ReadingHistory not found with id: " + nonExistentReadingHistoryId);

        verify(readingHistoryRepository).findById(nonExistentReadingHistoryId);
        verify(readingHistoryMapper, never()).toEntity(any(ReadingHistoryDTO.class));
        verify(readingHistoryRepository, never()).save(any(ReadingHistory.class));
        verify(readingHistoryMapper, never()).toDto(any(ReadingHistory.class));
    }

    @Test
    void deleteReadingHistory_WhenReadingHistoryExists_ShouldDeleteSuccessfully() {
        when(readingHistoryRepository.findById(readingHistoryId)).thenReturn(Optional.of(readingHistory));
        doNothing().when(readingHistoryRepository).deleteById(readingHistoryId);

        readingHistoryService.deleteReadingHistory(readingHistoryId);

        verify(readingHistoryRepository).findById(readingHistoryId);
        verify(readingHistoryRepository).deleteById(readingHistoryId);
    }

    @Test
    void deleteReadingHistory_WhenReadingHistoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(readingHistoryRepository.findById(nonExistentReadingHistoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            readingHistoryService.deleteReadingHistory(nonExistentReadingHistoryId);
        });

        assertThat(exception.getMessage()).contains("ReadingHistory not found with id: " + nonExistentReadingHistoryId);

        verify(readingHistoryRepository).findById(nonExistentReadingHistoryId);
        verify(readingHistoryRepository, never()).deleteById(anyLong());
    }
}
