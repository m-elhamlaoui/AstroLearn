package com.example.demo.integration;

import com.example.demo.dto.*;
import com.example.demo.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QuizServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private QuizService quizService;

    private Long quizId;
    private Long lessonId = 1L; // Mock lesson ID for testing

    @BeforeEach
    void setUp() {
        setUpTestUser();
        createTestQuiz();
    }

    private void createTestQuiz() {
        // Create a test quiz
        List<QuizQuestionDTO> questions = new ArrayList<>();
        
        // Question 1
        List<String> options1 = new ArrayList<>();
        options1.add("Option 1");
        options1.add("Option 2");
        options1.add("Option 3");
        options1.add("Option 4");
        
        QuizQuestionDTO question1 = new QuizQuestionDTO(
            null,
            "What is the closest planet to the Sun?",
            options1
        );
        
        // Question 2
        List<String> options2 = new ArrayList<>();
        options2.add("Option A");
        options2.add("Option B");
        options2.add("Option C");
        options2.add("Option D");
        
        QuizQuestionDTO question2 = new QuizQuestionDTO(
            null,
            "Which planet is known as the Red Planet?",
            options2
        );
        
        questions.add(question1);
        questions.add(question2);
        
        QuizDTO quizDTO = new QuizDTO(
            null,
            "Astronomy Basics Quiz",
            questions,
            lessonId,
            "Introduction to Astronomy",
            100 // Experience reward
        );
        
        QuizDTO savedQuiz = quizService.addOrUpdateQuizForLesson(lessonId, quizDTO, testUserId);
        quizId = savedQuiz.id();
    }

    @Test
    void shouldGetQuizForTaking() {
        // When
        QuizDTO quiz = quizService.getQuizForTaking(quizId);

        // Then
        assertThat(quiz).isNotNull();
        assertThat(quiz.title()).isEqualTo("Astronomy Basics Quiz");
        assertThat(quiz.questions()).hasSize(2);
        assertThat(quiz.questions().get(0).questionText()).isEqualTo("What is the closest planet to the Sun?");
    }

    @Test
    void shouldSubmitQuizAndGetCompletion() {
        // Given
        List<QuizQuestionAnswerDTO> answers = new ArrayList<>();
        
        // Assuming correct answers are at index 0 and 1
        answers.add(new QuizQuestionAnswerDTO(1L, 0)); // First question, first option
        answers.add(new QuizQuestionAnswerDTO(2L, 1)); // Second question, second option
        
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(testUserId, answers);

        // When
        QuizCompletionDTO completion = quizService.submitQuiz(testUserId, quizId, submissionDTO);

        // Then
        assertThat(completion).isNotNull();
        assertThat(completion.userId()).isEqualTo(testUserId);
        assertThat(completion.quizId()).isEqualTo(quizId);
        assertThat(completion.score()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldGetQuizCompletion() {
        // Given
        List<QuizQuestionAnswerDTO> answers = new ArrayList<>();
        answers.add(new QuizQuestionAnswerDTO(1L, 0));
        answers.add(new QuizQuestionAnswerDTO(2L, 1));
        
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(testUserId, answers);
        quizService.submitQuiz(testUserId, quizId, submissionDTO);

        // When
        QuizCompletionDTO completion = quizService.getQuizCompletion(testUserId, quizId);

        // Then
        assertThat(completion).isNotNull();
        assertThat(completion.userId()).isEqualTo(testUserId);
        assertThat(completion.quizId()).isEqualTo(quizId);
    }

    @Test
    void shouldGetQuizByLessonId() {
        // When
        QuizDTO quiz = quizService.getQuizByLessonId(lessonId);

        // Then
        assertThat(quiz).isNotNull();
        assertThat(quiz.lessonId()).isEqualTo(lessonId);
        assertThat(quiz.title()).isEqualTo("Astronomy Basics Quiz");
    }

    @Test
    void shouldDeleteQuizByLessonId() {
        // When
        quizService.deleteQuizByLessonId(lessonId, testUserId);

        // Then
        // Verify quiz is deleted by trying to get it (should throw exception)
        try {
            quizService.getQuizByLessonId(lessonId);
            // If we get here, the test should fail
            assertThat(true).isFalse();
        } catch (Exception e) {
            // Expected exception, test passes
            assertThat(true).isTrue();
        }
    }
} 