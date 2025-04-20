package com.example.demo.service.impl;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.LessonDTO;
import com.example.demo.dto.ModuleDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.model.Module;
import com.example.demo.repository.*;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EntityMapper entityMapper;

    // --- Course Methods ---
    @Override
    public CourseDTO createCourse(CourseDTO courseDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for creating course");

        Course course = entityMapper.toEntity(courseDTO);
        course.setModules(Collections.emptyList());
        course.setProgresses(Collections.emptyList());
        Course savedCourse = courseRepository.save(course);
        return entityMapper.toDTO(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return entityMapper.toDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for updating course " + id);

        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Update basic fields manually or via mapper update method if defined
        existingCourse.setTitle(courseDTO.title());
        existingCourse.setDescription(courseDTO.description());
        existingCourse.setDifficulty(courseDTO.difficulty());

        Course updatedCourse = courseRepository.save(existingCourse);
        return entityMapper.toDTO(updatedCourse);
    }

    @Override
    public void deleteCourse(Long id /*, Long adminUserId */) {
        // TODO: Add security check: Ensure performing user is ADMIN
        System.out.println("Placeholder: Security check needed for deleting course " + id);

        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }
        courseRepository.deleteById(id); // Cascade handles modules, lessons, etc.
    }
}