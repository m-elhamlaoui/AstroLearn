package com.example.demo.service.integration;

import com.example.demo.dto.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Course;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.model.Quiz;
import com.example.demo.model.QuizCompletion;
import com.example.demo.model.QuizQuestion;
import com.example.demo.model.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.QuizCompletionRepository;
import com.example.demo.repository.QuizQuestionRepository;
import com.example.demo.repository.QuizRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.QuizService;
import com.example.demo.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.util.BaseIntegrationTest;
import static com.example.demo.util.TestLogger.*;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuizServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizCompletionRepository quizCompletionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User adminUser;
    private Course course;
    private Module module;
    private Lesson lesson;
    private Quiz quiz;
    private QuizQuestion question1;
    private QuizQuestion question2;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for QuizServiceIntegrationTest");
        // Clean up before each test
        quizCompletionRepository.deleteAll();
        quizQuestionRepository.deleteAll();
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test users with different data from seeder
        testUser = new User();
        testUser.setUsername("quiz_learner");
        testUser.setEmail("learner@space.com");
        testUser.setPassword("quizpass123");
        testUser.setRole(User.UserRole.USER);
        testUser.setVerificationStatus(User.UserVerification.VERIFIED);
        testUser.setBio("Passionate about space quizzes");
        testUser.setProfileImageUrl("https://i.pravatar.cc/150?u=quizlearner");
        testUser.setPhotoCoverUrl("https://picsum.photos/seed/quizlearner/800/200");
        testUser.setExperiencePoints(2200);
        testUser = userRepository.save(testUser);
        entityManager.flush();

        adminUser = new User();
        adminUser.setUsername("quiz_admin");
        adminUser.setEmail("admin@space.com");
        adminUser.setPassword("adminpass123");
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setVerificationStatus(User.UserVerification.VERIFIED);
        adminUser.setBio("Space education administrator");
        adminUser.setProfileImageUrl("https://i.pravatar.cc/150?u=quizadmin");
        adminUser.setPhotoCoverUrl("https://picsum.photos/seed/quizadmin/800/200");
        adminUser.setExperiencePoints(5000);
        adminUser = userRepository.save(adminUser);
        entityManager.flush();

        // Create test course with different data from seeder
        course = new Course();
        course.setTitle("Space Technology Fundamentals");
        course.setDescription("Comprehensive overview of modern space technologies and their applications");
        course.setDifficulty(Course.DifficultyLevel.INTERMEDIATE);
        course.setImageUrl("https://picsum.photos/seed/spacetech/600/300");
        course = courseRepository.save(course);
        entityManager.flush();

        // Create test module with different data from seeder
        module = new Module();
        module.setTitle("Satellite Systems and Operations");
        module.setCourse(course);
        module = moduleRepository.save(module);
        entityManager.flush();

        // Create test lesson with different data from seeder
        lesson = new Lesson();
        lesson.setTitle("Satellite Communication Systems");
        lesson.setContent("Understanding the principles and technologies behind satellite communications");
        lesson.setVideoUrl("https://www.youtube.com/watch?v=satellite-comms");
        lesson.setModule(module);
        lesson = lessonRepository.save(lesson);
        entityManager.flush();

        // Create test quiz with different data from seeder
        quiz = new Quiz();
        quiz.setTitle("Satellite Technology Assessment");
        quiz.setLesson(lesson);
        quiz.setExperienceReward(150);
        quiz = quizRepository.save(quiz);
        entityManager.flush();

        // Create test questions with different data from seeder
        question1 = new QuizQuestion();
        question1.setQuiz(quiz);
        question1.setQuestionText("What is the primary function of a geostationary satellite?");
        question1.setOptions(new ArrayList<>(Arrays.asList(
            "Maintain constant position relative to Earth's surface",
            "Orbit the Earth multiple times per day",
            "Monitor deep space phenomena",
            "Transport astronauts to space stations"
        )));
        question1.setCorrectOptionIndex(0);
        question1 = quizQuestionRepository.save(question1);
        entityManager.flush();

        question2 = new QuizQuestion();
        question2.setQuiz(quiz);
        question2.setQuestionText("Which frequency band is commonly used for satellite TV broadcasting?");
        question2.setOptions(new ArrayList<>(Arrays.asList(
            "Ku-band",
            "AM radio band",
            "WiFi 2.4GHz band",
            "FM radio band"
        )));
        question2.setCorrectOptionIndex(0);
        question2 = quizQuestionRepository.save(question2);
        entityManager.flush();

        // Link questions to quiz
        quiz.setQuestions(new ArrayList<>(Arrays.asList(question1, question2)));
        quizRepository.save(quiz);
        entityManager.flush();

        // Link quiz to lesson
        lesson.setQuiz(quiz);
        lessonRepository.save(lesson);
        entityManager.flush();

        entityManager.clear();
    }

    @Test
    void testSubmitQuiz_CorrectAnswers() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(
                        new QuizQuestionAnswerDTO(question1.getId(), 0), // Correct for Q1
                        new QuizQuestionAnswerDTO(question2.getId(), 0)  // Correct for Q2
                )
        );

        QuizSubmissionResultDTO result = quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO);

        assertThat(result).isNotNull();
        assertThat(result.rawScore()).isEqualTo(2);
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.isPerfected()).isTrue();
        assertThat(result.experienceEarned()).isEqualTo(150);

        // Verify QuizCompletion in database
        Optional<QuizCompletion> completionOpt = quizCompletionRepository.findByUserIdAndQuizId(testUser.getId(), quiz.getId());
        assertThat(completionOpt).isPresent();
        assertThat(completionOpt.get().getScore()).isEqualTo(2);
        assertThat(completionOpt.get().getExperienceEarned()).isEqualTo(150);

        // Verify user experience points
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getExperiencePoints()).isEqualTo(2200 + 150);
    }

    @Test
    void testSubmitQuiz_PartialCorrectAnswers() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(
                        new QuizQuestionAnswerDTO(question1.getId(), 0), // Correct for Q1
                        new QuizQuestionAnswerDTO(question2.getId(), 1)  // Incorrect for Q2
                )
        );

        QuizSubmissionResultDTO result = quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO);

        assertThat(result).isNotNull();
        assertThat(result.rawScore()).isEqualTo(1);
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.isPerfected()).isFalse();
        assertThat(result.experienceEarned()).isEqualTo(150);

        // Verify QuizCompletion in database
        Optional<QuizCompletion> completionOpt = quizCompletionRepository.findByUserIdAndQuizId(testUser.getId(), quiz.getId());
        assertThat(completionOpt).isPresent();
        assertThat(completionOpt.get().getScore()).isEqualTo(1);
        assertThat(completionOpt.get().getExperienceEarned()).isEqualTo(150);

        // Verify user experience points
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getExperiencePoints()).isEqualTo(2200 + 150);
    }

    @Test
    void testSubmitQuiz_IncorrectAnswers() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(
                        new QuizQuestionAnswerDTO(question1.getId(), 1), // Incorrect for Q1
                        new QuizQuestionAnswerDTO(question2.getId(), 1)  // Incorrect for Q2
                )
        );

        QuizSubmissionResultDTO result = quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO);

        assertThat(result).isNotNull();
        assertThat(result.rawScore()).isEqualTo(0);
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.isPerfected()).isFalse();
        assertThat(result.experienceEarned()).isEqualTo(150);

        // Verify QuizCompletion in database
        Optional<QuizCompletion> completionOpt = quizCompletionRepository.findByUserIdAndQuizId(testUser.getId(), quiz.getId());
        assertThat(completionOpt).isPresent();
        assertThat(completionOpt.get().getScore()).isEqualTo(0);
        assertThat(completionOpt.get().getExperienceEarned()).isEqualTo(150);

        // Verify user experience points
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getExperiencePoints()).isEqualTo(2200 + 150);
    }

    @Test
    void testSubmitQuiz_UserNotFound() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                999L,
                Collections.emptyList()
        );
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.submitQuiz(999L, quiz.getId(), submissionDTO));
    }

    @Test
    void testSubmitQuiz_QuizNotFound() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                Collections.emptyList()
        );
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.submitQuiz(testUser.getId(), 999L, submissionDTO));
    }

    @Test
    void testSubmitQuiz_MismatchedAnswerCount() {
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(new QuizQuestionAnswerDTO(question1.getId(), 0)) // Only one answer
        );
        assertThrows(BadRequestException.class, () ->
                quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO));
    }

    @Test
    void testGetQuizCompletion() {
        // Submit a quiz first
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(
                        new QuizQuestionAnswerDTO(question1.getId(), 0), // Correct
                        new QuizQuestionAnswerDTO(question2.getId(), 1)  // Incorrect
                )
        );
        quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO);
        entityManager.flush();
        entityManager.clear();

        AugmentedQuizCompletionDTO completion = quizService.getQuizCompletion(testUser.getId(), quiz.getId());

        assertThat(completion).isNotNull();
        assertThat(completion.userId()).isEqualTo(testUser.getId());
        assertThat(completion.username()).isEqualTo(testUser.getUsername());
        assertThat(completion.quizId()).isEqualTo(quiz.getId());
        assertThat(completion.quizTitle()).isEqualTo(quiz.getTitle());
        assertThat(completion.rawScore()).isEqualTo(1);
        assertThat(completion.totalQuestions()).isEqualTo(2);
        assertThat(completion.experienceEarned()).isEqualTo(150);
        assertThat(completion.completionDate()).isNotNull();
        assertThat(completion.attemptDetails()).isEmpty();
    }

    @Test
    void testGetQuizCompletion_NotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.getQuizCompletion(testUser.getId(), 999L));
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.getQuizCompletion(999L, quiz.getId()));
    }

    @Test
    void testGetQuizForTaking() {
        QuizDTO quizForTaking = quizService.getQuizForTaking(quiz.getId());

        assertThat(quizForTaking).isNotNull();
        assertThat(quizForTaking.id()).isEqualTo(quiz.getId());
        assertThat(quizForTaking.title()).isEqualTo(quiz.getTitle());
        assertThat(quizForTaking.lessonId()).isEqualTo(lesson.getId());
        assertThat(quizForTaking.questions()).hasSize(2);
        assertThat(quizForTaking.questions().stream().map(QuizQuestionDTO::questionText))
                .containsExactlyInAnyOrder("What is the primary function of a geostationary satellite?", "Which frequency band is commonly used for satellite TV broadcasting?");
        assertThat(quizForTaking.questions().stream().map(QuizQuestionDTO::correctOptionIndex))
                .containsExactlyInAnyOrder(0, 0);
    }

    @Test
    void testGetQuizForTaking_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> quizService.getQuizForTaking(999L));
    }

    @Test
    void testAddOrUpdateQuizForLesson_Add() {
        // Create a new lesson without a quiz
        Lesson newLesson = new Lesson();
        newLesson.setTitle("Lesson without Quiz");
        newLesson.setContent("Content");
        newLesson.setModule(module);
        newLesson = lessonRepository.save(newLesson);
        entityManager.flush();
        entityManager.clear();

        QuizDTO newQuizDTO = new QuizDTO(
                null,
                "New Quiz for Lesson",
                List.of(
                        new QuizQuestionDTO(null, "New Q1?", List.of("A", "B"), 0),
                        new QuizQuestionDTO(null, "New Q2?", List.of("C", "D"), 1)
                ),
                newLesson.getId(),
                newLesson.getTitle(),
                50
        );

        QuizDTO createdQuiz = quizService.addOrUpdateQuizForLesson(newLesson.getId(), newQuizDTO, adminUser.getId());

        assertThat(createdQuiz).isNotNull();
        assertThat(createdQuiz.title()).isEqualTo("New Quiz for Lesson");
        assertThat(createdQuiz.lessonId()).isEqualTo(newLesson.getId());
        assertThat(createdQuiz.experienceReward()).isEqualTo(50);
        assertThat(createdQuiz.questions()).hasSize(2);

        // Verify in database
        Optional<Quiz> savedQuizOpt = quizRepository.findByLessonId(newLesson.getId());
        assertThat(savedQuizOpt).isPresent();
        assertThat(savedQuizOpt.get().getTitle()).isEqualTo("New Quiz for Lesson");
        assertThat(savedQuizOpt.get().getLesson().getId()).isEqualTo(newLesson.getId());
        assertThat(savedQuizOpt.get().getQuestions()).hasSize(2);
    }

    @Test
    void testAddOrUpdateQuizForLesson_Update() {
        // Quiz already exists from setup

        QuizDTO updatedQuizDTO = new QuizDTO(
                quiz.getId(),
                "Updated Quiz Title",
                List.of(
                        new QuizQuestionDTO(null, "Updated Q1?", List.of("X", "Y"), 0)
                ),
                lesson.getId(),
                lesson.getTitle(),
                150
        );

        QuizDTO updatedQuiz = quizService.addOrUpdateQuizForLesson(lesson.getId(), updatedQuizDTO, adminUser.getId());

        assertThat(updatedQuiz).isNotNull();
        assertThat(updatedQuiz.id()).isEqualTo(quiz.getId());
        assertThat(updatedQuiz.title()).isEqualTo("Updated Quiz Title");
        assertThat(updatedQuiz.experienceReward()).isEqualTo(150);
        assertThat(updatedQuiz.questions()).hasSize(1);

        // Verify in database
        Optional<Quiz> savedQuizOpt = quizRepository.findById(quiz.getId());
        assertThat(savedQuizOpt).isPresent();
        assertThat(savedQuizOpt.get().getTitle()).isEqualTo("Updated Quiz Title");
        assertThat(savedQuizOpt.get().getExperienceReward()).isEqualTo(150);
        assertThat(savedQuizOpt.get().getQuestions()).hasSize(1);
        assertThat(savedQuizOpt.get().getQuestions().get(0).getQuestionText()).isEqualTo("Updated Q1?");
    }

    @Test
    void testAddOrUpdateQuizForLesson_LessonNotFound() {
        QuizDTO newQuizDTO = new QuizDTO(
                null,
                "New Quiz",
                Collections.emptyList(),
                null,
                null,
                50
        );
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.addOrUpdateQuizForLesson(999L, newQuizDTO, adminUser.getId()));
    }

    @Test
    void testAddOrUpdateQuizForLesson_AdminUserNotFound() {
        QuizDTO newQuizDTO = new QuizDTO(
                null,
                "New Quiz",
                Collections.emptyList(),
                null,
                null,
                50
        );
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.addOrUpdateQuizForLesson(lesson.getId(), newQuizDTO, 999L));
    }

    @Test
    void testGetQuizByLessonId() {
        QuizDTO foundQuiz = quizService.getQuizByLessonId(lesson.getId());

        assertThat(foundQuiz).isNotNull();
        assertThat(foundQuiz.id()).isEqualTo(quiz.getId());
        assertThat(foundQuiz.title()).isEqualTo(quiz.getTitle());
        assertThat(foundQuiz.lessonId()).isEqualTo(lesson.getId());
    }

    @Test
    void testGetQuizByLessonId_LessonNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> quizService.getQuizByLessonId(999L));
    }

    @Test
    void testGetQuizByLessonId_QuizNotFoundForLesson() {
        // Create a lesson without a quiz
        Lesson tempLesson = new Lesson();
        tempLesson.setTitle("Lesson without Quiz");
        tempLesson.setContent("Content");
        tempLesson.setModule(module);
        final Lesson lessonWithoutQuiz = lessonRepository.save(tempLesson);
        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class, () ->
                quizService.getQuizByLessonId(lessonWithoutQuiz.getId()));
    }

    @Test
    void testDeleteQuizByLessonId() {
        Long lessonId = lesson.getId();
        Long quizId = quiz.getId();

        quizService.deleteQuizByLessonId(lessonId, adminUser.getId());

        // Verify quiz and questions are deleted
        assertThat(quizRepository.findById(quizId)).isEmpty();
        assertThat(quizQuestionRepository.findByQuizId(quizId)).isEmpty();

        // Verify lesson is updated (quiz link removed)
        Lesson updatedLesson = lessonRepository.findById(lessonId).orElseThrow();
        assertThat(updatedLesson.getQuiz()).isNull();
    }

    @Test
    void testDeleteQuizByLessonId_LessonNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.deleteQuizByLessonId(999L, adminUser.getId()));
    }

    @Test
    void testDeleteQuizByLessonId_AdminUserNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                quizService.deleteQuizByLessonId(lesson.getId(), 999L));
    }

    @Test
    void testDeleteQuizByLessonId_QuizNotFoundForLesson() {
        // Create a lesson without a quiz
        Lesson tempLesson = new Lesson();
        tempLesson.setTitle("Lesson without Quiz");
        tempLesson.setContent("Content");
        tempLesson.setModule(module);
        final Lesson lessonWithoutQuiz = lessonRepository.save(tempLesson);
        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class, () ->
                quizService.deleteQuizByLessonId(lessonWithoutQuiz.getId(), adminUser.getId()));
    }

    @Test
    void testGetAugmentedQuizCompletionById() {
        // Submit a quiz first to create a completion record
        QuizSubmissionDTO submissionDTO = new QuizSubmissionDTO(
                testUser.getId(),
                List.of(
                        new QuizQuestionAnswerDTO(question1.getId(), 0), // Correct
                        new QuizQuestionAnswerDTO(question2.getId(), 1)  // Incorrect
                )
        );
        QuizSubmissionResultDTO submissionResult = quizService.submitQuiz(testUser.getId(), quiz.getId(), submissionDTO);
        entityManager.flush();
        entityManager.clear();

        AugmentedQuizCompletionDTO augmentedCompletion = quizService.getAugmentedQuizCompletionById(submissionResult.completionId());

        assertThat(augmentedCompletion).isNotNull();
        assertThat(augmentedCompletion.id()).isEqualTo(submissionResult.completionId());
        assertThat(augmentedCompletion.rawScore()).isEqualTo(1);
        assertThat(augmentedCompletion.totalQuestions()).isEqualTo(2);
        assertThat(augmentedCompletion.userId()).isEqualTo(testUser.getId());
        assertThat(augmentedCompletion.username()).isEqualTo(testUser.getUsername());
        assertThat(augmentedCompletion.quizId()).isEqualTo(quiz.getId());
        assertThat(augmentedCompletion.quizTitle()).isEqualTo(quiz.getTitle());
        assertThat(augmentedCompletion.experienceEarned()).isEqualTo(150);
        assertThat(augmentedCompletion.completionDate()).isNotNull();
        assertThat(augmentedCompletion.attemptDetails()).hasSize(2);

        // Verify attempt details structure
        assertThat(augmentedCompletion.attemptDetails().stream().map(d -> d.questionId())).containsExactlyInAnyOrder(question1.getId(), question2.getId());
        assertThat(augmentedCompletion.attemptDetails().stream().allMatch(d -> d.chosenOptionIndex() == -1)).isTrue();
        assertThat(augmentedCompletion.attemptDetails().stream().map(d -> d.correctOptionIndex())).containsExactlyInAnyOrder(0, 0);
    }

    @Test
    void testGetAugmentedQuizCompletionById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> quizService.getAugmentedQuizCompletionById(999L));
    }
}
