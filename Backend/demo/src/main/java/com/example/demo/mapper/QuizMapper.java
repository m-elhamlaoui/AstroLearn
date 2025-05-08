package com.example.demo.mapper;

import com.example.demo.dto.QuizDTO;
import com.example.demo.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    QuizDTO toDto(Quiz quiz);

    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "questions", ignore = true)
    Quiz toEntity(QuizDTO quizDTO);
}