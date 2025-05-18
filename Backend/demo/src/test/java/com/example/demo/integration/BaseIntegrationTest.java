package com.example.demo.integration;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de base pour tous les tests d'intégration.
 * Fournit la configuration commune et les utilitaires partagés.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    protected Long testUserId;
    protected User testUser;

    /**
     * Initialise un utilisateur de test avec des privilèges d'administrateur.
     * Appelé avant chaque test.
     */
    protected void setUpTestUser() {
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.UserRole.ADMIN);
        testUser.setVerificationStatus(User.UserVerification.VERIFIED);
        
        User savedUser = userRepository.save(testUser);
        testUserId = savedUser.getId();
    }

    /**
     * Initialise un utilisateur de test avec des privilèges limités.
     * Utile pour tester les restrictions d'accès.
     */
    protected void setUpRegularUser() {
        // Create a regular user
        User regularUser = new User();
        regularUser.setUsername("regularuser");
        regularUser.setEmail("regular@example.com");
        regularUser.setPassword("password");
        regularUser.setRole(User.UserRole.USER);
        regularUser.setVerificationStatus(User.UserVerification.VERIFIED);
        
        User savedUser = userRepository.save(regularUser);
        testUserId = savedUser.getId();
        testUser = savedUser;
    }
} 