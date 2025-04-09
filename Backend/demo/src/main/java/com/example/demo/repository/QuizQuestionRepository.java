package com.example.demo.repository;


import com.example.demo.model.QuizQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    @EntityGraph(attributePaths = {"quiz"})
    List<QuizQuestion> findByQuizId(Long quizId);

    @Query("SELECT q FROM QuizQuestion q WHERE q.quiz.lesson.id = :lessonId")
    List<QuizQuestion> findByLessonId(@Param("lessonId") Long lessonId);

    @Modifying
    @Query("DELETE FROM QuizQuestion q WHERE q.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT q.correctOptionIndex FROM QuizQuestion q WHERE q.id = :questionId")
    Integer findCorrectAnswerIndex(@Param("questionId") Long questionId);
}