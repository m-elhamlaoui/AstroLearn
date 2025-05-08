package com.example.demo.mapper;

import com.example.demo.dto.CourseDTO;
import com.example.demo.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper extends BaseMapper {
    @Mapping(target = "moduleIds", source = "modules", qualifiedByName = "modulesToModuleIds")
    @Mapping(target = "totalLessons", expression = "java(course.getTotalLessons())")
    CourseDTO toDto(Course course);

    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "progresses", ignore = true)
    @Mapping(target = "totalLessons", ignore = true)
    Course toEntity(CourseDTO courseDTO);
}