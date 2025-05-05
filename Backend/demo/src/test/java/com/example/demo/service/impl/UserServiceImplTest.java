package com.example.demo.service.impl;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private EntityMapper entityMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setBio("Test bio");
        user.setProfileImageUrl("profile.jpg");
        user.setPhotoCoverUrl("cover.jpg");
        user.setRole(User.UserRole.USER);
        user.setVerificationStatus(User.UserVerification.UNVERIFIED);
        user.setLevel(User.UserLevel.NOVICE);
        user.setExperiencePoints(50);
        user.setArticleCount(2L);
        user.setCommentCount(3L);
        user.setQuizCompletionCount(1L);

        userDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                "Test bio",
                null,
                "profile.jpg",
                "cover.jpg",
                User.UserRole.USER,
                User.UserVerification.UNVERIFIED,
                User.UserLevel.NOVICE,
                50,
                2L,
                3L,
                1L,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("JUnit test for getUserById method - Success")
    void givenUserId_whenGetUserById_thenReturnUserDTO() {
        // Arrange
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(entityMapper.toDTO(user)).willReturn(userDTO);

        // Act
        UserDTO foundUserDTO = userService.getUserById(1L);

        // Assert
        assertThat(foundUserDTO).isNotNull();
        assertThat(foundUserDTO).isEqualTo(userDTO);
        verify(userRepository).findById(1L);
        verify(entityMapper).toDTO(user);
    }

    @Test
    @DisplayName("JUnit test for getUserById method - Not Found")
    void givenNonExistentUserId_whenGetUserById_thenThrowsResourceNotFoundException() {
        // Arrange
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : '99'");
        verify(userRepository).findById(99L);
        verifyNoInteractions(entityMapper);
    }

    @Test
    @DisplayName("JUnit test for createUser method - Success")
    void givenUserDTOForCreation_whenCreateUser_thenReturnSavedUserDTO() {
        // Arrange
        UserDTO createUserDTO = new UserDTO(
                null,
                "newuser",
                "new@example.com",
                "New Bio",
                "rawPassword123",
                "new_profile.jpg",
                "new_cover.jpg",
                null,
                null,
                null,
                0,
                null, null, null,
                null, null, null, null
        );

        User userToSave = new User();
        userToSave.setUsername("newuser");
        userToSave.setEmail("new@example.com");
        userToSave.setBio("New Bio");
        userToSave.setProfileImageUrl("new_profile.jpg");
        userToSave.setPhotoCoverUrl("new_cover.jpg");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword("encodedNewPassword");
        savedUser.setBio("New Bio");
        savedUser.setProfileImageUrl("new_profile.jpg");
        savedUser.setPhotoCoverUrl("new_cover.jpg");
        savedUser.setRole(User.UserRole.USER);
        savedUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        savedUser.setLevel(User.UserLevel.NOVICE);
        savedUser.setExperiencePoints(0);
        savedUser.setArticleCount(0L);
        savedUser.setCommentCount(0L);
        savedUser.setQuizCompletionCount(0L);

        UserDTO expectedSavedUserDTO = new UserDTO(
                2L, "newuser", "new@example.com", "New Bio", null, "new_profile.jpg", "new_cover.jpg",
                User.UserRole.USER, User.UserVerification.UNVERIFIED, User.UserLevel.NOVICE, 0,
                0L, 0L, 0L,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );

        // Configure mocks
        given(userRepository.existsByUsername("newuser")).willReturn(false);
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(entityMapper.toEntity(createUserDTO)).willReturn(userToSave);
        given(passwordEncoder.encode("rawPassword123")).willReturn("encodedNewPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(entityMapper.toDTO(savedUser)).willReturn(expectedSavedUserDTO);

        // Act
        UserDTO resultDTO = userService.createUser(createUserDTO);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO).isEqualTo(expectedSavedUserDTO);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userPassedToSave = userArgumentCaptor.getValue();

        assertThat(userPassedToSave.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(userPassedToSave.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(userPassedToSave.getVerificationStatus()).isEqualTo(User.UserVerification.UNVERIFIED);
        assertThat(userPassedToSave.getLevel()).isEqualTo(User.UserLevel.NOVICE);
        assertThat(userPassedToSave.getExperiencePoints()).isZero();

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(entityMapper).toEntity(createUserDTO);
        verify(passwordEncoder).encode("rawPassword123");
        verify(entityMapper).toDTO(savedUser);
    }

    @Test
    @DisplayName("JUnit test for createUser method - Username Exists")
    void givenExistingUsername_whenCreateUser_thenThrowsBadRequestException() {
        // Arrange
        UserDTO createUserDTO = new UserDTO(
            null, "testuser", "new@example.com", "New Bio", "rawPassword123",
            null, null, null, null, null, 0,
            null, null, null, null, null, null, null
        );
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists: testuser");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verifyNoInteractions(entityMapper);
        verify(userRepository, never()).save(any(User.class));
    }
}