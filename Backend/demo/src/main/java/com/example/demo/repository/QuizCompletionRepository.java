package com.example.demo.repository;

import com.example.demo.model.QuizCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizCompletionRepository extends JpaRepository<QuizCompletion, Long> {

    // This method checks if a quiz completion exists for a specific user and quiz
    boolean existsByUserIdAndQuizId(Long userId, Long quizId);

    List<QuizCompletion> findByUserId(Long userId);
}