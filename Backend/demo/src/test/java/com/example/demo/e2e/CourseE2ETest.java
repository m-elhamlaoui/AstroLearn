package com.example.demo.e2e;

import com.example.demo.dto.CourseRequest;
import com.example.demo.payloadRequest.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CourseE2ETest extends BaseE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        setUpTestUser();
    }

    @Test
    public void testCreateAndGetCourse() throws Exception {
        // Create course
        CourseRequest courseRequest = new CourseRequest();
        courseRequest.setTitle("Test Course");
        courseRequest.setDescription("Test Description");
        courseRequest.setPrice(99.99);

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String courseId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Get course
        mockMvc.perform(get("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Course"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    public void testUpdateCourse() throws Exception {
        // Create course first
        CourseRequest createRequest = new CourseRequest();
        createRequest.setTitle("Original Title");
        createRequest.setDescription("Original Description");
        createRequest.setPrice(49.99);

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String courseId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Update course
        CourseRequest updateRequest = new CourseRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(79.99);

        mockMvc.perform(put("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(79.99));
    }

    @Test
    public void testDeleteCourse() throws Exception {
        // Create course first
        CourseRequest createRequest = new CourseRequest();
        createRequest.setTitle("Course to Delete");
        createRequest.setDescription("Will be deleted");
        createRequest.setPrice(29.99);

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String courseId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Delete course
        mockMvc.perform(delete("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify course is deleted
        mockMvc.perform(get("/api/courses/" + courseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}