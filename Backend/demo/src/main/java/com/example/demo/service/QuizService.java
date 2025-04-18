package com.example.demo.service;

import com.example.demo.dto.QuizCompletionDTO;
import com.example.demo.dto.QuizDTO; // For return type if needed
import com.example.demo.dto.QuizSubmissionDTO; // Input DTO

public interface QuizService {

    /*
     * Submits answers for a quiz, calculates the score, records completion,
     * and awards experience points to the user.
     * @param userId User ID submitting the quiz
     * @param quizId Quiz ID being submitted
     * @param submissionDTO DTO containing the list of answers
     * @return QuizCompletionDTO representing the result
     * @throws ResourceNotFoundException if user or quiz not found
     * @throws BadRequestException if submission is invalid (e.g., wrong number of answers)
     */
    QuizCompletionDTO submitQuiz(Long userId, Long quizId, QuizSubmissionDTO submissionDTO);

    /*
     * Gets a user's completion record for a specific quiz.
     * @param userId User ID
     * @param quizId Quiz ID
     * @return QuizCompletionDTO
     * @throws ResourceNotFoundException if completion record not found
     */
    QuizCompletionDTO getQuizCompletion(Long userId, Long quizId);

    /*
     * Gets the quiz details (needed for taking the quiz).
     * Should fetch the quiz and its questions, but exclude correct answers in the DTO mapping.
     * @param quizId Quiz ID
     * @return QuizDTO (without correct answers)
     * @throws ResourceNotFoundException if quiz not found
     */
    QuizDTO getQuizForTaking(Long quizId); // Renamed to be specific

}
