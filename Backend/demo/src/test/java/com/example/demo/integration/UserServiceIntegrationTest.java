package com.example.demo.integration;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        UserDTO userDTO = new UserDTO(
                null, // id will be generated
                "newuser",
                "newuser@example.com",
                "Bio for new user",
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
                new ArrayList<>());

        // When
        UserDTO savedUser = userService.createUser(userDTO);

        // Then
        assertThat(savedUser.id()).isNotNull();
        UserDTO foundUser = userService.getUserById(savedUser.id());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.username()).isEqualTo("newuser");
        assertThat(foundUser.email()).isEqualTo("newuser@example.com");
    }

    @Test
    void shouldGetUserByUsername() {
        // Given
        UserDTO userDTO = new UserDTO(
                null,
                "usernameuser",
                "username@example.com",
                "Bio",
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
                new ArrayList<>());

        userService.createUser(userDTO);

        // When
        UserDTO foundUser = userService.getUserByUsername("usernameuser");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.username()).isEqualTo("usernameuser");
    }

    @Test
    void shouldGetUserByEmail() {
        // Given
        UserDTO userDTO = new UserDTO(
                null,
                "emailuser",
                "email@example.com",
                "Bio",
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
                new ArrayList<>());

        userService.createUser(userDTO);

        // When
        UserDTO foundUser = userService.getUserByEmail("email@example.com");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.email()).isEqualTo("email@example.com");
    }

    @Test
    void shouldListAllUsers() {
        // Given
        UserDTO user1 = new UserDTO(
                null,
                "user1",
                "user1@example.com",
                "Bio 1",
                "password",
                "profile1.jpg",
                "cover1.jpg",
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
                new ArrayList<>());

        UserDTO user2 = new UserDTO(
                null,
                "user2",
                "user2@example.com",
                "Bio 2",
                "password",
                "profile2.jpg",
                "cover2.jpg",
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
                new ArrayList<>());

        userService.createUser(user1);
        userService.createUser(user2);

        // When
        List<UserDTO> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
        assertThat(users).extracting(UserDTO::username)
                .contains("user1", "user2");
    }

    @Test
    void shouldUpdateUser() {
        // Given
        UserDTO originalUser = new UserDTO(
                null,
                "updateuser",
                "update@example.com",
                "Original Bio",
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
                new ArrayList<>());

        UserDTO savedUser = userService.createUser(originalUser);

        // When
        UserDTO updatedUserDTO = new UserDTO(
                savedUser.id(),
                savedUser.username(),
                savedUser.email(),
                "Updated Bio",
                savedUser.password(),
                "updated-profile.jpg",
                savedUser.photoCoverUrl(),
                savedUser.role(),
                savedUser.verificationStatus(),
                savedUser.level(),
                savedUser.experiencePoints(),
                savedUser.articleCount(),
                savedUser.commentCount(),
                savedUser.quizCompletionCount(),
                savedUser.readingHistoryIds(),
                savedUser.courseProgressIds(),
                savedUser.quizCompletionIds(),
                savedUser.createdSpaceMissionIds());

        UserDTO updatedUser = userService.updateUser(savedUser.id(), updatedUserDTO);

        // Then
        assertThat(updatedUser.bio()).isEqualTo("Updated Bio");
        assertThat(updatedUser.profileImageUrl()).isEqualTo("updated-profile.jpg");
    }

    @Test
    void shouldAddExperiencePoints() {
        // Given
        UserDTO userDTO = new UserDTO(
                null,
                "expuser",
                "exp@example.com",
                "Bio",
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
                new ArrayList<>());

        UserDTO savedUser = userService.createUser(userDTO);

        // When
        userService.addExperiencePoints(savedUser.id(), 1500);

        // Then
        UserDTO updatedUser = userService.getUserById(savedUser.id());
        assertThat(updatedUser.experiencePoints()).isEqualTo(1500);
        assertThat(updatedUser.level()).isEqualTo(User.UserLevel.EXPLORER);
    }

    @Test
    void shouldHandleVerificationProcess() {
        // Given
        UserDTO userDTO = new UserDTO(
                null,
                "verifyuser",
                "verify@example.com",
                "Bio",
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
                new ArrayList<>());

        UserDTO savedUser = userService.createUser(userDTO);

        // When requesting verification
        userService.requestVerification(savedUser.id());
        UserDTO pendingUser = userService.getUserById(savedUser.id());
        assertThat(pendingUser.verificationStatus()).isEqualTo(User.UserVerification.PENDING);

        // When approving verification
        userService.approveVerification(savedUser.id());
        UserDTO verifiedUser = userService.getUserById(savedUser.id());
        assertThat(verifiedUser.verificationStatus()).isEqualTo(User.UserVerification.VERIFIED);
    }

    @Test
    void shouldGetUsersByVerificationStatus() {
        // Given
        UserDTO user1 = new UserDTO(
                null,
                "verifieduser",
                "verified@example.com",
                "Bio",
                "password",
                "profile.jpg",
                "cover.jpg",
                User.UserRole.USER,
                User.UserVerification.VERIFIED,
                User.UserLevel.NOVICE,
                0,
                0L,
                0L,
                0L,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        UserDTO user2 = new UserDTO(
                null,
                "pendinguser",
                "pending@example.com",
                "Bio",
                "password",
                "profile.jpg",
                "cover.jpg",
                User.UserRole.USER,
                User.UserVerification.PENDING,
                User.UserLevel.NOVICE,
                0,
                0L,
                0L,
                0L,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        // Create users and ensure they are persisted
        UserDTO savedUser1 = userService.createUser(user1);
        UserDTO savedUser2 = userService.createUser(user2);

        // When
        List<UserDTO> verifiedUsers = userService.getUsersByVerificationStatus(User.UserVerification.VERIFIED);
        List<UserDTO> pendingUsers = userService.getUsersByVerificationStatus(User.UserVerification.PENDING);

        // Then
        assertThat(verifiedUsers).hasSize(1);
        assertThat(verifiedUsers).extracting(UserDTO::username).contains("verifieduser");
        assertThat(pendingUsers).hasSize(1);
        assertThat(pendingUsers).extracting(UserDTO::username).contains("pendinguser");
    }
}