package com.example.demo.service.impl;

import com.example.demo.dto.QuizDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.QuizMapper;
import com.example.demo.model.Quiz;
import com.example.demo.repository.QuizRepository;
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
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizMapper quizMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz quiz;
    private QuizDTO quizDTO;
    private Long quizId = 1L;
    private Long nonExistentQuizId = 999L;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setDescription("Test Description");

        quizDTO = new QuizDTO();
        quizDTO.setId(quizId);
        quizDTO.setTitle("Test Quiz");
        quizDTO.setDescription("Test Description");
    }

    @Test
    void getQuizById_WhenQuizExists_ShouldReturnQuizDTO() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizMapper.toDto(quiz)).thenReturn(quizDTO);

        QuizDTO result = quizService.getQuizById(quizId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(quizId);
        assertThat(result.getTitle()).isEqualTo(quizDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(quizDTO.getDescription());

        verify(quizRepository).findById(quizId);
        verify(quizMapper).toDto(quiz);
    }

    @Test
    void getQuizById_WhenQuizDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(quizRepository.findById(nonExistentQuizId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            quizService.getQuizById(nonExistentQuizId);
        });

        assertThat(exception.getMessage()).contains("Quiz not found with id: " + nonExistentQuizId);

        verify(quizRepository).findById(nonExistentQuizId);
        verify(quizMapper, never()).toDto(any(Quiz.class));
    }

    @Test
    void getAllQuizzes_ShouldReturnListOfQuizDTOs() {
        List<Quiz> quizzes = Arrays.asList(quiz);
        List<QuizDTO> quizDTOs = Arrays.asList(quizDTO);

        when(quizRepository.findAll()).thenReturn(quizzes);
        when(quizMapper.toDto(quiz)).thenReturn(quizDTO);

        List<QuizDTO> result = quizService.getAllQuizzes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(quizId);
        assertThat(result.get(0).getTitle()).isEqualTo(quizDTO.getTitle());
        assertThat(result.get(0).getDescription()).isEqualTo(quizDTO.getDescription());

        verify(quizRepository).findAll();
        verify(quizMapper).toDto(quiz);
    }

    @Test
    void createQuiz_ShouldReturnCreatedQuizDTO() {
        when(quizMapper.toEntity(quizDTO)).thenReturn(quiz);
        when(quizRepository.save(quiz)).thenReturn(quiz);
        when(quizMapper.toDto(quiz)).thenReturn(quizDTO);

        QuizDTO result = quizService.createQuiz(quizDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(quizId);
        assertThat(result.getTitle()).isEqualTo(quizDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(quizDTO.getDescription());

        verify(quizMapper).toEntity(quizDTO);
        verify(quizRepository).save(quiz);
        verify(quizMapper).toDto(quiz);
    }

    @Test
    void updateQuiz_WhenQuizExists_ShouldReturnUpdatedQuizDTO() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizMapper.toEntity(quizDTO)).thenReturn(quiz);
        when(quizRepository.save(quiz)).thenReturn(quiz);
        when(quizMapper.toDto(quiz)).thenReturn(quizDTO);

        QuizDTO result = quizService.updateQuiz(quizId, quizDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(quizId);
        assertThat(result.getTitle()).isEqualTo(quizDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(quizDTO.getDescription());

        verify(quizRepository).findById(quizId);
        verify(quizMapper).toEntity(quizDTO);
        verify(quizRepository).save(quiz);
        verify(quizMapper).toDto(quiz);
    }

    @Test
    void updateQuiz_WhenQuizDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(quizRepository.findById(nonExistentQuizId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            quizService.updateQuiz(nonExistentQuizId, quizDTO);
        });

        assertThat(exception.getMessage()).contains("Quiz not found with id: " + nonExistentQuizId);

        verify(quizRepository).findById(nonExistentQuizId);
        verify(quizMapper, never()).toEntity(any(QuizDTO.class));
        verify(quizRepository, never()).save(any(Quiz.class));
        verify(quizMapper, never()).toDto(any(Quiz.class));
    }

    @Test
    void deleteQuiz_WhenQuizExists_ShouldDeleteSuccessfully() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        doNothing().when(quizRepository).deleteById(quizId);

        quizService.deleteQuiz(quizId);

        verify(quizRepository).findById(quizId);
        verify(quizRepository).deleteById(quizId);
    }

    @Test
    void deleteQuiz_WhenQuizDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(quizRepository.findById(nonExistentQuizId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            quizService.deleteQuiz(nonExistentQuizId);
        });

        assertThat(exception.getMessage()).contains("Quiz not found with id: " + nonExistentQuizId);

        verify(quizRepository).findById(nonExistentQuizId);
        verify(quizRepository, never()).deleteById(anyLong());
    }
}
