package com.example.demo.service.integration;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.util.BaseIntegrationTest;
import static com.example.demo.util.TestLogger.*;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for UserServiceIntegrationTest");
        // Clean up before each test
        userRepository.deleteAll();

        // Create a test user DTO
        testUserDTO = new UserDTO(
                null, // id
                "testuser", // username
                "test@example.com", // email
                "Test Bio", // bio
                "password", // password
                "http://example.com/profile.jpg", // profileImageUrl
                "http://example.com/cover.jpg", // photoCoverUrl
                User.UserRole.USER, // role
                User.UserVerification.VERIFIED, // verificationStatus
                User.UserLevel.NOVICE, // level
                0, // experiencePoints
                null, // articleCount
                null, // commentCount
                null, // quizCompletionCount
                new ArrayList<>(), // readingHistoryIds
                new ArrayList<>(), // courseProgressIds
                new ArrayList<>(), // quizCompletionIds
                new ArrayList<>() // createdSpaceMissionIds
        );
    }

    @Test
    void testCreateUser() {
        UserDTO createdUser = userService.createUser(testUserDTO);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.username()).isEqualTo("testuser");
        assertThat(createdUser.email()).isEqualTo("test@example.com");
        assertThat(createdUser.password()).isNotEqualTo("password"); // Password should be hashed
        assertThat(createdUser.password()).isNotNull(); // Password should be hashed, not null
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        userService.createUser(testUserDTO);
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserDTO));
    }

    @Test
    void testGetUserById() {
        UserDTO createdUser = userService.createUser(testUserDTO);
        UserDTO foundUser = userService.getUserById(createdUser.id());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.id()).isEqualTo(createdUser.id());
        assertThat(foundUser.username()).isEqualTo("testuser");
    }

    @Test
    void testGetUserById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testUpdateUser() {
        UserDTO createdUser = userService.createUser(testUserDTO);
        UserDTO updatedUserDTO = new UserDTO(
                createdUser.id(), // id
                "updateduser", // username
                "updated@example.com", // email
                "Updated Bio", // bio
                "newpassword", // password
                "http://example.com/updated-profile.jpg", // profileImageUrl
                "http://example.com/updated-cover.jpg", // photoCoverUrl
                User.UserRole.USER, // role
                User.UserVerification.VERIFIED, // verificationStatus
                User.UserLevel.NOVICE, // level
                0, // experiencePoints
                null, // articleCount
                null, // commentCount
                null, // quizCompletionCount
                new ArrayList<>(), // readingHistoryIds
                new ArrayList<>(), // courseProgressIds
                new ArrayList<>(), // quizCompletionIds
                new ArrayList<>() // createdSpaceMissionIds
        );
        UserDTO updatedUser = userService.updateUser(createdUser.id(), updatedUserDTO);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.username()).isEqualTo("updateduser");
        assertThat(updatedUser.email()).isEqualTo("updated@example.com");
    }

    @Test
    void testUpdateUser_NotFound() {
        UserDTO updatedUserDTO = new UserDTO(
                999L, // id
                "updateduser", // username
                "updated@example.com", // email
                "Updated Bio", // bio
                "newpassword", // password
                "http://example.com/updated-profile.jpg", // profileImageUrl
                "http://example.com/updated-cover.jpg", // photoCoverUrl
                User.UserRole.USER, // role
                User.UserVerification.VERIFIED, // verificationStatus
                User.UserLevel.NOVICE, // level
                0, // experiencePoints
                null, // articleCount
                null, // commentCount
                null, // quizCompletionCount
                new ArrayList<>(), // readingHistoryIds
                new ArrayList<>(), // courseProgressIds
                new ArrayList<>(), // quizCompletionIds
                new ArrayList<>() // createdSpaceMissionIds
        );
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, updatedUserDTO));
    }

    @Test
    void testDeleteUser() {
        UserDTO createdUser = userService.createUser(testUserDTO);
        userService.deleteUser(createdUser.id());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(createdUser.id()));
    }

    @Test
    void testDeleteUser_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
    }
} 
