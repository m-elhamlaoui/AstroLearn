package com.example.demo.mapper;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.model.CourseProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseProgressMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "currentLessonId", source = "currentLesson.id")
    CourseProgressDTO toDto(CourseProgress courseProgress);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "currentLesson", ignore = true)
    CourseProgress toEntity(CourseProgressDTO courseProgressDTO);
}