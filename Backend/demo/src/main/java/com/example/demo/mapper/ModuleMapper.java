package com.example.demo.mapper;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.model.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModuleMapper extends BaseMapper {
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonIds", source = "lessons", qualifiedByName = "lessonsToLessonIds")
    ModuleDTO toDto(Module module);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "lessonCount", ignore = true)
    Module toEntity(ModuleDTO moduleDTO);
}