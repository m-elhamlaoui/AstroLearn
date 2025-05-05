package com.example.demo.e2e;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Classe de base pour tous les tests E2E.
 * Fournit la configuration commune et les utilitaires partagés.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseE2ETest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserService userService;

    protected String authToken;
    protected Long userId;

    /**
     * Crée un utilisateur de test et obtient un token d'authentification.
     * Appelé avant chaque test.
     */
    protected void setUpTestUser() throws Exception {
        // Créer un utilisateur de test
        UserDTO userDTO = new UserDTO(
            null,
            "testuser",
            "test@example.com",
            "Test Bio",
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

        UserDTO savedUser = userService.createUser(userDTO);
        userId = savedUser.id();

        // Obtenir un token d'authentification
        String loginJson = objectMapper.writeValueAsString(new LoginRequest("testuser", "password"));
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        authToken = loginResponse.token();
    }

    /**
     * Classe interne pour la requête de connexion
     */
    protected static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    /**
     * Classe interne pour la réponse de connexion
     */
    protected static class LoginResponse {
        public String token;
        public String type = "Bearer";
        public Long id;
        public String username;
        public String email;
        public String role;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
} 