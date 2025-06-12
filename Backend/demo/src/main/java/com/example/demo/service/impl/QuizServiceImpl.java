package com.example.demo.service.impl;

import com.example.demo.dto.AugmentedQuizCompletionDTO;
import com.example.demo.dto.QuizAttemptDetailDTO;
import com.example.demo.dto.QuizCompletionDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.dto.QuizQuestionAnswerDTO;
import com.example.demo.dto.QuizSubmissionDTO;
import com.example.demo.dto.QuizSubmissionResultDTO; // Import the new DTO
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
import java.util.ArrayList;
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
    public QuizSubmissionResultDTO submitQuiz(Long userId, Long quizId, QuizSubmissionDTO submissionDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // 1. Fetch Quiz
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        // 2. Fetch associated Questions separately
        // Assuming QuizQuestionRepository has findByQuizId method
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId); 
        if (questions == null) {
             questions = Collections.emptyList();
        }
        // Optional: Associate fetched questions in memory if needed elsewhere, 
        // but not strictly required for DTO creation below.
        // quiz.setQuestions(questions); 

        // 3. Explicitly initialize options for the fetched questions
        for (QuizQuestion question : questions) {
            if (question.getOptions() != null) {
                question.getOptions().size(); // Trigger initialization
            } else {
                 // Handle case where options might be unexpectedly null in DB?
                 question.setOptions(Collections.emptyList()); // Avoid NPE later if needed
            }
        }
        
        // 4. Build map for quick lookup
        Map<Long, QuizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        if (submissionDTO.answers() == null || submissionDTO.answers().size() != questions.size()) {
            throw new BadRequestException("Number of answers (" + (submissionDTO.answers() == null ? 0 : submissionDTO.answers().size())
                    + ") does not match number of questions (" + questions.size() + ").");
        }

        int rawScore = 0;
        // The attemptDetails list will be populated first, ensuring all data is copied from entities
        List<QuizAttemptDetailDTO> populatedAttemptDetails = new ArrayList<>();

        for (QuizQuestionAnswerDTO userAnswer : submissionDTO.answers()) {
            QuizQuestion question = questionMap.get(userAnswer.questionId());
            if (question == null) {
                // This case should ideally be prevented by validation or indicate a data integrity issue
                // For now, we'll skip or handle as an error, depending on desired strictness
                // Or throw new BadRequestException("Submitted answer for non-existent question ID: " + userAnswer.questionId());
                continue;
            }

            // Ensure options are loaded by accessing them here before creating the DTO
            List<String> currentQuestionOptions = question.getOptions();
            if (currentQuestionOptions == null) {
                currentQuestionOptions = Collections.emptyList(); // Should not happen with EAGER or if initialized
            }


            boolean isCorrect = userAnswer.chosenOptionIndex() == question.getCorrectOptionIndex();
            if (isCorrect) {
                rawScore++;
            }

            populatedAttemptDetails.add(new QuizAttemptDetailDTO(
                    question.getId(),
                    question.getQuestionText(),
                    new ArrayList<>(currentQuestionOptions), // Create a new list to be safe
                    userAnswer.chosenOptionIndex(),
                    question.getCorrectOptionIndex(),
                    isCorrect
            ));
        }

        QuizCompletion completion = quizCompletionRepository.findByUserIdAndQuizId(userId, quizId).orElse(null);

        if (completion == null) {
            completion = new QuizCompletion();
            completion.setUser(user);
            completion.setQuiz(quiz);
        }
        completion.setScore(rawScore); // Use rawScore
        completion.setExperienceEarned(quiz.getExperienceReward()); // Set experience earned
        completion.setCompletionDate(LocalDateTime.now());
        QuizCompletion savedCompletion = quizCompletionRepository.save(completion);

        if (quiz.getExperienceReward() > 0) {
            userService.addExperiencePoints(userId, quiz.getExperienceReward());
        }

        boolean isPerfected = rawScore == questions.size();

        return new QuizSubmissionResultDTO(
                savedCompletion.getId(),
                rawScore,
                questions.size(),
                isPerfected,
                quiz.getExperienceReward() // Assuming experience is awarded on any attempt, or adjust if only on perfection
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AugmentedQuizCompletionDTO getQuizCompletion(Long userId, Long quizId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        QuizCompletion completion = quizCompletionRepository.findByUserIdAndQuizId(userId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "QuizCompletion not found for user " + userId + " and quiz " + quizId));
        
        Quiz quiz = completion.getQuiz();
        if (quiz == null) { // Should not happen if data integrity is maintained
             throw new IllegalStateException("QuizCompletion with id " + completion.getId() + " has a null Quiz reference.");
        }

        // Fetch the full quiz details to get the questions for totalQuestions count
        // This ensures we have the question list even if it's lazily loaded on the Quiz entity from completion.getQuiz()
        Quiz fullQuiz = quizRepository.findByIdWithQuestions(quiz.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quiz.getId())); // Should not happen if quiz from completion is valid

        User completedByUser = completion.getUser();
        if (completedByUser == null) { // Should not happen
            throw new IllegalStateException("QuizCompletion with id " + completion.getId() + " has a null User reference.");
        }


        return new AugmentedQuizCompletionDTO(
                completion.getId(),
                completion.getScore(),
                fullQuiz.getQuestions() != null ? fullQuiz.getQuestions().size() : 0,
                completion.getCompletionDate(),
                completedByUser.getId(), 
                completedByUser.getUsername(), 
                quiz.getId(), 
                quiz.getTitle(), 
                quiz.getExperienceReward(),
                Collections.emptyList() // Historical attempt details are not stored
        );
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
            List<QuizQuestion> newQuestions = quizDTO.questions().stream()
                    .map(qDto -> {
                        QuizQuestion question = entityMapper.toEntity(qDto);
                        question.setQuiz(savedQuiz); // Link to saved quiz
                        return question;
                    }).collect(Collectors.toList());
            quizQuestionRepository.saveAll(newQuestions); // Save questions
            savedQuiz.setQuestions(newQuestions); // Update collection in memory
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

    @Override
    @Transactional(readOnly = true)
    public AugmentedQuizCompletionDTO getAugmentedQuizCompletionById(Long completionId) {
        QuizCompletion completion = quizCompletionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizCompletion", "id", completionId));

        User user = completion.getUser();
        Quiz quiz = completion.getQuiz();

        if (user == null || quiz == null) {
            // This indicates a data integrity issue if a completion record exists without user or quiz
            throw new IllegalStateException("QuizCompletion record " + completionId + " is missing user or quiz association.");
        }

        // Fetch questions for the quiz, ensuring options are loaded
        // QuizQuestion.options is EAGER, but explicit initialization is safer if issues persist.
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());
        if (questions == null) {
            questions = Collections.emptyList();
        }
        for (QuizQuestion q : questions) {
            if (q.getOptions() != null) {
                q.getOptions().size(); // Initialize options
            } else {
                q.setOptions(Collections.emptyList());
            }
        }

        List<QuizAttemptDetailDTO> attemptDetails = new ArrayList<>();
        for (QuizQuestion question : questions) {
            // For a historical completion, we don't have the user's specific chosenOptionIndex for this attempt
            // unless it was persisted. We'll use a placeholder.
            attemptDetails.add(new QuizAttemptDetailDTO(
                    question.getId(),
                    question.getQuestionText(),
                    new ArrayList<>(question.getOptions()), // Create a new list
                    -1, // Placeholder for chosenOptionIndex as it's not stored with QuizCompletion
                    question.getCorrectOptionIndex(),
                    false // Cannot determine isCorrect without chosenOptionIndex
            ));
        }

        return new AugmentedQuizCompletionDTO(
                completion.getId(),
                completion.getScore(),
                questions.size(),
                completion.getCompletionDate(),
                user.getId(),
                user.getUsername(),
                quiz.getId(),
                quiz.getTitle(),
                quiz.getExperienceReward(),
                attemptDetails
        );
    }
}
