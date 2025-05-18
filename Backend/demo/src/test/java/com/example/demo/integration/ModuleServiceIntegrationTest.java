package com.example.demo.integration;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.dto.CourseDTO;
import com.example.demo.model.Course;
import com.example.demo.service.ModuleService;
import com.example.demo.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private CourseService courseService;

    private Long courseId;
    private Long moduleId;

    @BeforeEach
    void setUp() {
        setUpTestUser();
        createTestCourse();
        createTestModule();
    }

    private void createTestCourse() {
        CourseDTO courseDTO = new CourseDTO(
                null,
                "Test Course",
                "A test course for integration testing",
                Course.DifficultyLevel.BEGINNER,
                0,
                new ArrayList<>());

        CourseDTO savedCourse = courseService.createCourse(courseDTO);
        courseId = savedCourse.id();
    }

    private void createTestModule() {
        ModuleDTO moduleDTO = new ModuleDTO(
                null,
                "Introduction to Astronomy",
                courseId,
                0,
                new ArrayList<>());

        ModuleDTO savedModule = moduleService.addModuleToCourse(courseId, moduleDTO);
        moduleId = savedModule.id();
    }

    @Test
    void shouldAddModuleToCourse() {
        // Given
        ModuleDTO newModuleDTO = new ModuleDTO(
                null,
                "Advanced Astronomy",
                courseId,
                0,
                new ArrayList<>());

        // When
        ModuleDTO savedModule = moduleService.addModuleToCourse(courseId, newModuleDTO);

        // Then
        assertThat(savedModule).isNotNull();
        assertThat(savedModule.id()).isNotNull();
        assertThat(savedModule.title()).isEqualTo("Advanced Astronomy");
        assertThat(savedModule.courseId()).isEqualTo(courseId);
    }

    @Test
    void shouldGetModuleById() {
        // When
        ModuleDTO module = moduleService.getModuleById(moduleId);

        // Then
        assertThat(module).isNotNull();
        assertThat(module.id()).isEqualTo(moduleId);
        assertThat(module.title()).isEqualTo("Introduction to Astronomy");
        assertThat(module.courseId()).isEqualTo(courseId);
    }

    @Test
    void shouldUpdateModule() {
        // Given
        ModuleDTO updatedModuleDTO = new ModuleDTO(
                moduleId,
                "Updated Astronomy Introduction",
                courseId,
                0,
                new ArrayList<>());

        // When
        ModuleDTO updatedModule = moduleService.updateModule(moduleId, updatedModuleDTO);

        // Then
        assertThat(updatedModule).isNotNull();
        assertThat(updatedModule.id()).isEqualTo(moduleId);
        assertThat(updatedModule.title()).isEqualTo("Updated Astronomy Introduction");
        assertThat(updatedModule.courseId()).isEqualTo(courseId);
    }

    @Test
    void shouldGetModulesByCourseId() {
        // When
        List<ModuleDTO> modules = moduleService.getModulesByCourseId(courseId);

        // Then
        assertThat(modules).isNotEmpty();
        assertThat(modules).hasSize(1);
        assertThat(modules.get(0).id()).isEqualTo(moduleId);
        assertThat(modules.get(0).title()).isEqualTo("Introduction to Astronomy");
    }

    @Test
    void shouldDeleteModule() {
        // When
        moduleService.deleteModule(moduleId);

        // Then
        // Verify module is deleted by trying to get it (should throw exception)
        try {
            moduleService.getModuleById(moduleId);
            // If we get here, the test should fail
            assertThat(true).isFalse();
        } catch (Exception e) {
            // Expected exception, test passes
            assertThat(true).isTrue();
        }
    }
}