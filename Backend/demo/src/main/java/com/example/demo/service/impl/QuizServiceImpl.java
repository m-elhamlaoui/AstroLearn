package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.QuizService;
import com.example.demo.service.UserService; // To add experience points
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizCompletionRepository quizCompletionRepository;
    private final UserRepository userRepository;
    private final UserService userService; // Use service to add points
    private final EntityMapper entityMapper;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;


    @Override
    public QuizCompletionDTO submitQuiz(Long userId, Long quizId, QuizSubmissionDTO submissionDTO) {
        // Ensure requesting user ID matches userId

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId) // Use fetch join method
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        List<QuizQuestion> questions = quiz.getQuestions();
        Map<Long, Integer> correctAnswers = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, QuizQuestion::getCorrectOptionIndex));

        if (submissionDTO.answers() == null || submissionDTO.answers().size() != questions.size()) {
            throw new BadRequestException("Number of answers (" + (submissionDTO.answers() == null ? 0 : submissionDTO.answers().size())
                    + ") does not match number of questions (" + questions.size() + ").");
        }

        int score = 0;
        for (QuizQuestionAnswerDTO answer : submissionDTO.answers()) {
            Integer correctIndex = correctAnswers.get(answer.questionId());
            if (correctIndex != null && answer.chosenOptionIndex() == correctIndex) {
                score++;
            }
        }

        // Check if completion already exists
        QuizCompletion completion = quizCompletionRepository.findByUserIdAndQuizId(userId, quizId).orElse(null);

        if (completion == null) {
            // Create new completion record
            completion = new QuizCompletion();
            completion.setUser(user);
            completion.setQuiz(quiz);
        } else {
            // Update existing completion record (if retakes are allowed)
            completion.setCompletionDate(LocalDateTime.now());
        }

        // Update score and save
        completion.setScore(score);
        QuizCompletion savedCompletion = quizCompletionRepository.save(completion);
        if (quiz.getExperienceReward() > 0) {
            userService.addExperiencePoints(userId, quiz.getExperienceReward());
        }

        return entityMapper.toDTO(savedCompletion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizCompletionDTO getQuizCompletion(Long userId, Long quizId) {
        // Ensure requesting user ID matches userId

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        QuizCompletion completion = quizCompletionRepository.findByUserIdAndQuizId(userId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "QuizCompletion not found for user " + userId + " and quiz " + quizId));
        return entityMapper.toDTO(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizForTaking(Long quizId) {
        // Anyone can potentially view a quiz structure to take it
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId) // Use fetch join method
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        // Mapper ignores correctOptionIndex for QuizQuestionDTO
        return entityMapper.toDTO(quiz);
    }

    // --- Quiz Methods ---
    @Override
    public QuizDTO addOrUpdateQuizForLesson(Long lessonId, QuizDTO quizDTO , Long adminUserId ) {

        // Ensure performing user is ADMIN
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));
        System.out.println("Placeholder: Security check needed for adding/updating quiz for lesson " + lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        Quiz quiz;
        Optional<Quiz> existingQuizOpt = quizRepository.findByLessonId(lessonId); // Assumes findByLessonId exists

        if (existingQuizOpt.isPresent()) {
            quiz = existingQuizOpt.get();
            quiz.setTitle(quizDTO.title());
            quiz.setExperienceReward(quizDTO.experienceReward());
            // Efficiently replace questions: delete existing, then add new
            quizQuestionRepository.deleteAll(quiz.getQuestions()); // Or let orphanRemoval handle if configured
            quiz.getQuestions().clear();
        } else {
            quiz = new Quiz();
            quiz.setLesson(lesson);
            quiz.setTitle(quizDTO.title());
            quiz.setExperienceReward(quizDTO.experienceReward());
        }

        Quiz savedQuiz = quizRepository.save(quiz); // Save quiz first

        // Add questions
        if (quizDTO.questions() != null) {
            List<QuizQuestion> questions = quizDTO.questions().stream()
                    .map(qDto -> {
                        QuizQuestion question = entityMapper.toEntity(qDto);
                        question.setQuiz(savedQuiz); // Link to saved quiz
                        return question;
                    }).collect(Collectors.toList());
            quizQuestionRepository.saveAll(questions); // Save questions
            savedQuiz.setQuestions(questions); // Update collection in memory
        } else {
            savedQuiz.setQuestions(Collections.emptyList());
        }

        // Link quiz back to lesson if new
        if (existingQuizOpt.isEmpty()) {
            lesson.setQuiz(savedQuiz);
            lessonRepository.save(lesson);
        }

        return entityMapper.toDTO(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizByLessonId(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        Quiz quiz = quizRepository.findByLessonId(lessonId) // Assumes findByLessonId exists
                .orElseThrow(() -> new ResourceNotFoundException("Quiz for Lesson", "lessonId", lessonId));
        return entityMapper.toDTO(quiz);
    }

    @Override
    public void deleteQuizByLessonId(Long lessonId , Long adminUserId) {
        //Ensure performing user is ADMIN
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        System.out.println("Placeholder: Security check needed for deleting quiz for lesson " + lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        Quiz quiz = quizRepository.findByLessonId(lessonId) // Assumes findByLessonId exists
                .orElseThrow(() -> new ResourceNotFoundException("Quiz for Lesson", "lessonId", lessonId));

        lesson.setQuiz(null); // Unlink first
        lessonRepository.save(lesson);
        quizRepository.delete(quiz); // Cascade handles questions
    }
}
