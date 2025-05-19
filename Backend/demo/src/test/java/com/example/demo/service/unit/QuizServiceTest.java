package com.example.demo.service.unit;

import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.UserService;
import com.example.demo.service.impl.QuizServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizCompletionRepository quizCompletionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitQuiz_Success() {
        Long userId = 1L;
        Long quizId = 1L;
        List<QuizQuestionAnswerDTO> answers = List.of(
            new QuizQuestionAnswerDTO(1L, 0),
            new QuizQuestionAnswerDTO(2L, 1)
        );
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(userId,answers);

        User user = new User();
        user.setId(userId);

        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setExperienceReward(100);

        QuizQuestion question1 = new QuizQuestion();
        question1.setId(1L);
        question1.setQuestionText("Question 1");
        question1.setOptions(List.of("Option 1", "Option 2"));
        question1.setCorrectOptionIndex(0);

        QuizQuestion question2 = new QuizQuestion();
        question2.setId(2L);
        question2.setQuestionText("Question 2");
        question2.setOptions(List.of("Option 1", "Option 2"));
        question2.setCorrectOptionIndex(1);

        List<QuizQuestion> questions = List.of(question1, question2);

        QuizCompletion completion = new QuizCompletion();
        completion.setId(1L);
        completion.setUser(user);
        completion.setQuiz(quiz);
        completion.setScore(2);
        completion.setCompletionDate(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizId(quizId)).thenReturn(questions);
        when(quizCompletionRepository.findByUserIdAndQuizId(userId, quizId)).thenReturn(Optional.empty());
        when(quizCompletionRepository.save(any(QuizCompletion.class))).thenReturn(completion);
        doNothing().when(userService).addExperiencePoints(userId, quiz.getExperienceReward());

        QuizSubmissionResultDTO result = quizService.submitQuiz(userId, quizId, submissionDTO);

        assertNotNull(result);
        assertEquals(2, result.rawScore());
        assertEquals(2, result.totalQuestions());
        assertTrue(result.isPerfected());
        assertEquals(100, result.experienceEarned());
        verify(userRepository, times(1)).findById(userId);
        verify(quizRepository, times(1)).findById(quizId);
        verify(quizQuestionRepository, times(1)).findByQuizId(quizId);
        verify(quizCompletionRepository, times(1)).save(any(QuizCompletion.class));
        verify(userService, times(1)).addExperiencePoints(userId, quiz.getExperienceReward());
    }

    @Test
    void testSubmitQuiz_UserNotFound() {
        Long userId = 1L;
        Long quizId = 1L;
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(userId,Collections.emptyList());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.submitQuiz(userId, quizId, submissionDTO));
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(quizRepository, quizQuestionRepository, quizCompletionRepository, userService);
    }

    @Test
    void testSubmitQuiz_QuizNotFound() {
        Long userId = 1L;
        Long quizId = 1L;
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(userId,Collections.emptyList());

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.submitQuiz(userId, quizId, submissionDTO));
        verify(userRepository, times(1)).findById(userId);
        verify(quizRepository, times(1)).findById(quizId);
        verifyNoMoreInteractions(quizQuestionRepository, quizCompletionRepository, userService);
    }

    @Test
    void testGetQuizCompletion_Success() {
        Long userId = 1L;
        Long quizId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setExperienceReward(100);

        QuizCompletion completion = new QuizCompletion();
        completion.setId(1L);
        completion.setUser(user);
        completion.setQuiz(quiz);
        completion.setScore(2); // Ensure score is set correctly
        completion.setCompletionDate(LocalDateTime.now());

        QuizQuestion question1 = new QuizQuestion();
        question1.setId(1L);
        question1.setQuestionText("Question 1");
        question1.setOptions(List.of("Option 1", "Option 2"));
        question1.setCorrectOptionIndex(0);

        QuizQuestion question2 = new QuizQuestion();
        question2.setId(2L);
        question2.setQuestionText("Question 2");
        question2.setOptions(List.of("Option 1", "Option 2"));
        question2.setCorrectOptionIndex(1);

        List<QuizQuestion> questions = List.of(question1, question2);
        quiz.setQuestions(questions); // Ensure questions are set in the quiz

        AugmentedQuizCompletionDTO expectedDTO = new AugmentedQuizCompletionDTO(
                1L, 2, 2, completion.getCompletionDate(),
                userId, "testuser", quizId, "Test Quiz", 100,
                Collections.emptyList()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(quizCompletionRepository.findByUserIdAndQuizId(userId, quizId)).thenReturn(Optional.of(completion));
        when(quizRepository.findByIdWithQuestions(quizId)).thenReturn(Optional.of(quiz));

        AugmentedQuizCompletionDTO result = quizService.getQuizCompletion(userId, quizId);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.rawScore(), result.rawScore());
        assertEquals(expectedDTO.totalQuestions(), result.totalQuestions());
        assertEquals(expectedDTO.userId(), result.userId());
        assertEquals(expectedDTO.quizId(), result.quizId());
        verify(userRepository, times(1)).findById(userId);
        verify(quizCompletionRepository, times(1)).findByUserIdAndQuizId(userId, quizId);
        verify(quizRepository, times(1)).findByIdWithQuestions(quizId);
    }

    @Test
    void testGetQuizForTaking_Success() {
        Long quizId = 1L;
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setExperienceReward(100);

        List<QuizQuestion> questions = List.of(
            new QuizQuestion(),
            new QuizQuestion()
        );
        quiz.setQuestions(questions);

        QuizDTO expectedDTO = new QuizDTO(quizId, "Test Quiz", Collections.emptyList(), null, null, 100);

        when(quizRepository.findByIdWithQuestions(quizId)).thenReturn(Optional.of(quiz));
        when(entityMapper.toDTO(quiz)).thenReturn(expectedDTO);

        QuizDTO result = quizService.getQuizForTaking(quizId);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        verify(quizRepository, times(1)).findByIdWithQuestions(quizId);
        verify(entityMapper, times(1)).toDTO(quiz);
    }

    @Test
    void testGetQuizForTaking_NotFound() {
        Long quizId = 1L;
        when(quizRepository.findByIdWithQuestions(quizId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.getQuizForTaking(quizId));
        verify(quizRepository, times(1)).findByIdWithQuestions(quizId);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void testAddOrUpdateQuizForLesson_Success() {
        Long lessonId = 1L;
        Long adminUserId = 1L;
        QuizDTO quizDTO = new QuizDTO(null, "Test Quiz", Collections.emptyList(), lessonId, "Test Lesson", 100);

        User adminUser = new User();
        adminUser.setId(adminUserId);
        adminUser.setRole(User.UserRole.ADMIN);

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle(quizDTO.title());
        quiz.setExperienceReward(quizDTO.experienceReward());
        quiz.setLesson(lesson);

        QuizDTO expectedDTO = new QuizDTO(1L, "Test Quiz", Collections.emptyList(), lessonId, "Test Lesson", 100);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(quizRepository.findByLessonId(lessonId)).thenReturn(Optional.empty());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
        when(entityMapper.toDTO(quiz)).thenReturn(expectedDTO);

        QuizDTO result = quizService.addOrUpdateQuizForLesson(lessonId, quizDTO, adminUserId);

        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.experienceReward(), result.experienceReward());
        verify(userRepository, times(1)).findById(adminUserId);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(quizRepository, times(1)).findByLessonId(lessonId);
        verify(quizRepository, times(1)).save(any(Quiz.class));
        verify(entityMapper, times(1)).toDTO(quiz);
    }

    @Test
    void testDeleteQuizByLessonId_Success() {
        Long lessonId = 1L;
        Long adminUserId = 1L;

        User adminUser = new User();
        adminUser.setId(adminUserId);
        adminUser.setRole(User.UserRole.ADMIN);

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setLesson(lesson);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(quizRepository.findByLessonId(lessonId)).thenReturn(Optional.of(quiz));


        quizService.deleteQuizByLessonId(lessonId, adminUserId);

        verify(userRepository, times(1)).findById(adminUserId);
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(quizRepository, times(1)).findByLessonId(lessonId);
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(quizRepository, times(1)).delete(any(Quiz.class));
    }
}