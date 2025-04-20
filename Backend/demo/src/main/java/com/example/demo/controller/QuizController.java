package com.example.demo.controller;

import com.example.demo.dto.QuizCompletionDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.dto.QuizSubmissionDTO;
import com.example.demo.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Submit a quiz
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizCompletionDTO> submitQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId,
            @RequestBody QuizSubmissionDTO submissionDTO) {
        QuizCompletionDTO completion = quizService.submitQuiz(userId, quizId, submissionDTO);
        return ResponseEntity.ok(completion);
    }

    // Get quiz completion record
    @GetMapping("/{quizId}/completion")
    public ResponseEntity<QuizCompletionDTO> getQuizCompletion(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        QuizCompletionDTO completion = quizService.getQuizCompletion(userId, quizId);
        return ResponseEntity.ok(completion);
    }

    // Get quiz details for taking
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuizForTaking(@PathVariable Long quizId) {
        QuizDTO quiz = quizService.getQuizForTaking(quizId);
        return ResponseEntity.ok(quiz);
    }

    // Add or update a quiz for a lesson
    @PostMapping("/lessons/{lessonId}")
    public ResponseEntity<QuizDTO> addOrUpdateQuizForLesson(
            @PathVariable Long lessonId,
            @RequestBody QuizDTO quizDTO,
            @RequestParam Long adminUserId) {
        QuizDTO updatedQuiz = quizService.addOrUpdateQuizForLesson(lessonId, quizDTO, adminUserId);
        return ResponseEntity.ok(updatedQuiz);
    }

    // Get quiz by lesson ID
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<QuizDTO> getQuizByLessonId(@PathVariable Long lessonId) {
        QuizDTO quiz = quizService.getQuizByLessonId(lessonId);
        return ResponseEntity.ok(quiz);
    }

    // Delete quiz by lesson ID
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteQuizByLessonId(
            @PathVariable Long lessonId,
            @RequestParam Long adminUserId) {
        quizService.deleteQuizByLessonId(lessonId, adminUserId);
        return ResponseEntity.noContent().build();
    }
}