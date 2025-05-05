package com.example.demo.e2e;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationE2ETest extends BaseE2ETest {

    @Test
    void shouldRegisterNewUser() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO(
            null,
            "newuser",
            "newuser@example.com",
            "New User Bio",
            "password",
            "profile.jpg",
            "cover.jpg",
            User.UserRole.USER,
            User.UserVerification.UNVERIFIED,
            User.UserLevel.NOVICE,
            0,
            0L,
            0L,
            0L,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );

        String userJson = objectMapper.writeValueAsString(userDTO);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }
} 