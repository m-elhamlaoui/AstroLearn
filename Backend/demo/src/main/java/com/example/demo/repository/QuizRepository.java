package com.example.demo.repository;

import com.example.demo.model.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// QuizRepository.java
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonId(Long lessonId);

    // This method retrieves a quiz with it associated questions
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findWithQuestionsById(Long id);
}
