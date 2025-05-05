package com.example.demo.e2e;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.ModuleDTO;
import com.example.demo.model.Course;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseE2ETest extends BaseE2ETest {

    private Long courseId;
    private Long moduleId;

    @BeforeEach
    void setUp() throws Exception {
        setUpTestUser();
        createTestCourse();
    }

    private void createTestCourse() throws Exception {
        // Create a test course
        CourseDTO courseDTO = new CourseDTO(
            null,
            "Introduction to Astronomy",
            "A comprehensive introduction to the basics of astronomy",
            Course.DifficultyLevel.BEGINNER,
            0,
            new ArrayList<>()
        );

        String courseJson = objectMapper.writeValueAsString(courseDTO);

        String response = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Introduction to Astronomy"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CourseDTO savedCourse = objectMapper.readValue(response, CourseDTO.class);
        courseId = savedCourse.id();

        // Create a test module for the course
        createTestModule();
    }

    private void createTestModule() throws Exception {
        ModuleDTO moduleDTO = new ModuleDTO(
            null,
            "Basic Concepts",
            courseId,
            0,
            new ArrayList<>()
        );

        String moduleJson = objectMapper.writeValueAsString(moduleDTO);

        String response = mockMvc.perform(post("/api/courses/" + courseId + "/modules")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(moduleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Basic Concepts"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ModuleDTO savedModule = objectMapper.readValue(response, ModuleDTO.class);
        moduleId = savedModule.id();
    }

    @Test
    void shouldGetCourseById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.title").value("Introduction to Astronomy"));
    }

    @Test
    void shouldUpdateCourse() throws Exception {
        // Given
        CourseDTO updatedCourseDTO = new CourseDTO(
            courseId,
            "Updated Astronomy Course",
            "Updated description",
            Course.DifficultyLevel.INTERMEDIATE,
            0,
            new ArrayList<>()
        );

        String courseJson = objectMapper.writeValueAsString(updatedCourseDTO);

        // When & Then
        mockMvc.perform(put("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Astronomy Course"))
                .andExpect(jsonPath("$.difficulty").value("INTERMEDIATE"));
    }

    @Test
    void shouldGetModulesByCourseId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/courses/" + courseId + "/modules")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(moduleId))
                .andExpect(jsonPath("$[0].title").value("Basic Concepts"));
    }

    @Test
    void shouldUpdateModule() throws Exception {
        // Given
        ModuleDTO updatedModuleDTO = new ModuleDTO(
            moduleId,
            "Updated Basic Concepts",
            courseId,
            0,
            new ArrayList<>()
        );

        String moduleJson = objectMapper.writeValueAsString(updatedModuleDTO);

        // When & Then
        mockMvc.perform(put("/api/modules/" + moduleId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(moduleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Basic Concepts"));
    }

    @Test
    void shouldDeleteModule() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/modules/" + moduleId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify module is deleted
        mockMvc.perform(get("/api/modules/" + moduleId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCourse() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify course is deleted
        mockMvc.perform(get("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
} 