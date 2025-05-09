package com.example.demo.service;

import com.example.demo.dto.AugmentedQuizCompletionDTO;
import com.example.demo.dto.QuizDTO; // For return type if needed
import com.example.demo.dto.QuizSubmissionDTO; // Input DTO
import com.example.demo.dto.QuizSubmissionResultDTO; // Import the new DTO

public interface QuizService {

    /*
     * Submits answers for a quiz, calculates the score, records completion,
     * and awards experience points to the user.
     * @param userId User ID submitting the quiz
     * @param quizId Quiz ID being submitted
     * @param submissionDTO DTO containing the list of answers
     * @return QuizSubmissionResultDTO containing the completion ID, score, and perfection status.
     * @throws ResourceNotFoundException if user or quiz not found
     * @throws BadRequestException if submission is invalid (e.g., wrong number of answers)
     */
    QuizSubmissionResultDTO submitQuiz(Long userId, Long quizId, QuizSubmissionDTO submissionDTO);

    /*
     * Gets a user's completion record for a specific quiz.
     * @param userId User ID
     * @param quizId Quiz ID
     * @return AugmentedQuizCompletionDTO
     * @throws ResourceNotFoundException if completion record not found
     */
    AugmentedQuizCompletionDTO getQuizCompletion(Long userId, Long quizId);

    /*
     * Gets the quiz details (needed for taking the quiz).
     * Should fetch the quiz and its questions, but exclude correct answers in the DTO mapping.
     * @param quizId Quiz ID
     * @return QuizDTO (without correct answers)
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO getQuizForTaking(Long quizId); // Renamed to be specific

    // --- Quiz Operations ---

    QuizDTO addOrUpdateQuizForLesson(Long lessonId, QuizDTO quizDTO, Long adminUserId); // Adds or updates a quiz for a lesson (admin function)
    QuizDTO getQuizByLessonId(Long lessonId);
    void deleteQuizByLessonId(Long lessonId, Long adminUserId); // Deletes a quiz by lesson ID (admin function)

    /*
     * Gets the detailed quiz completion data, including individual question attempts.
     * @param completionId The ID of the QuizCompletion record.
     * @return AugmentedQuizCompletionDTO containing detailed results.
     * @throws ResourceNotFoundException if completion record not found.
     */
    AugmentedQuizCompletionDTO getAugmentedQuizCompletionById(Long completionId);

}
