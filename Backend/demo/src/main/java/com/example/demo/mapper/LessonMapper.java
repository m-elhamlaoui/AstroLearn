package com.example.demo.mapper;

import com.example.demo.dto.LessonDTO;
import com.example.demo.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "quizId", source = "quiz.id")
    LessonDTO toDto(Lesson lesson);

    @Mapping(target = "module", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    Lesson toEntity(LessonDTO lessonDTO);
}