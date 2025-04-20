package com.example.demo.service.impl;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
// No need to import UserService interface for testing the Impl
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections; // For empty lists
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
// No need for willDoNothing unless testing void methods
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private EntityMapper entityMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO; // Represents the DTO typically returned by GET requests

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword"); // Entity has encoded password
        user.setBio("Test bio");
        user.setProfileImageUrl("profile.jpg");
        user.setPhotoCoverUrl("cover.jpg");
        user.setRole(User.UserRole.USER);
        user.setVerificationStatus(User.UserVerification.UNVERIFIED);
        user.setLevel(User.UserLevel.NOVICE);
        user.setExperiencePoints(50);
        // Assume @Formula fields are populated when entity is fetched/mapped
        user.setArticleCount(2L);
        user.setCommentCount(3L);
        user.setQuizCompletionCount(1L);

        // --- Corrected UserDTO instantiation for GET responses ---
        // Password should typically be null or excluded in DTOs returned to client
        userDTO = new UserDTO(
                1L,                             // id
                "testuser",                     // username
                "test@example.com",             // email
                "Test bio",                     // bio
                null,                           // password (IMPORTANT: SHOULD BE NULL/EXCLUDED in response DTO)
                "profile.jpg",                  // profileImageUrl
                "cover.jpg",                    // photoCoverUrl
                User.UserRole.USER,             // role
                User.UserVerification.UNVERIFIED, // verificationStatus
                User.UserLevel.NOVICE,          // level
                50,                             // experiencePoints
                2L,                             // articleCount
                3L,                             // commentCount
                1L,                             // quizCompletionCount
                Collections.emptyList(),        // readingHistoryIds (Assume empty for basic test)
                Collections.emptyList(),        // courseProgressIds (Assume empty)
                Collections.emptyList(),        // quizCompletionIds (Assume empty)
                Collections.emptyList()         // createdSpaceMissionIds (Assume empty)
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
        assertThat(foundUserDTO).isEqualTo(userDTO); // Compare the whole DTO
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
        // --- Corrected UserDTO for CREATION ---
        // Assumes password field holds the RAW password for creation request
        UserDTO createUserDTO = new UserDTO(
                null,                   // id (null for creation)
                "newuser",              // username
                "new@example.com",      // email
                "New Bio",              // bio
                "rawPassword123",       // password (RAW)
                "new_profile.jpg",      // profileImageUrl
                "new_cover.jpg",        // photoCoverUrl
                null,                   // role (will be defaulted)
                null,                   // verificationStatus (will be defaulted)
                null,                   // level (will be defaulted)
                0,                      // experiencePoints (will be defaulted)
                null, null, null,       // calculated counts (null on creation)
                null, null, null, null // ID lists (null on creation)
        );

        User userToSave = new User(); // Entity mapped from DTO, before defaults/encoding
        userToSave.setUsername("newuser");
        userToSave.setEmail("new@example.com");
        userToSave.setBio("New Bio");
        userToSave.setProfileImageUrl("new_profile.jpg");
        userToSave.setPhotoCoverUrl("new_cover.jpg");
        // Password not set here, mapper doesn't map it, service encodes

        User savedUser = new User(); // Entity object *after* save (with ID, encoded pass, defaults)
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword("encodedNewPassword"); // Assume this is the encoded result
        savedUser.setBio("New Bio");
        savedUser.setProfileImageUrl("new_profile.jpg");
        savedUser.setPhotoCoverUrl("new_cover.jpg");
        savedUser.setRole(User.UserRole.USER);
        savedUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        savedUser.setLevel(User.UserLevel.NOVICE);
        savedUser.setExperiencePoints(0);
        // @Formula fields might be null or 0 immediately after save depending on transaction state
        savedUser.setArticleCount(0L);
        savedUser.setCommentCount(0L);
        savedUser.setQuizCompletionCount(0L);

        // --- Corrected expected result DTO (matches GET DTO structure) ---
        // Password MUST be null here.
        UserDTO expectedSavedUserDTO = new UserDTO(
                2L, "newuser", "new@example.com", "New Bio", null, "new_profile.jpg", "new_cover.jpg",
                User.UserRole.USER, User.UserVerification.UNVERIFIED, User.UserLevel.NOVICE, 0,
                0L, 0L, 0L, // Counts might be 0
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList() // Lists likely empty
        );

        // Configure mocks
        given(userRepository.existsByUsername("newuser")).willReturn(false);
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(entityMapper.toEntity(createUserDTO)).willReturn(userToSave); // Map DTO -> Entity (without encoded pass)
        given(passwordEncoder.encode("rawPassword123")).willReturn("encodedNewPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser); // Mock save returns the final entity state
        given(entityMapper.toDTO(savedUser)).willReturn(expectedSavedUserDTO); // Mock mapping final entity -> DTO

        // Act
        UserDTO resultDTO = userService.createUser(createUserDTO);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO).isEqualTo(expectedSavedUserDTO); // Compare DTOs

        // Capture argument passed to save to verify encoding and defaults
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userPassedToSave = userArgumentCaptor.getValue();

        assertThat(userPassedToSave.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(userPassedToSave.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(userPassedToSave.getVerificationStatus()).isEqualTo(User.UserVerification.UNVERIFIED);
        assertThat(userPassedToSave.getLevel()).isEqualTo(User.UserLevel.NOVICE);
        assertThat(userPassedToSave.getExperiencePoints()).isZero();

        // Verify mock interactions
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
        // --- Corrected UserDTO for creation attempt ---
        UserDTO createUserDTO = new UserDTO(null, "testuser", "new@example.com", "New Bio", "rawPassword123", null, null, null, null, null, 0, null, null, null, null, null, null, null);
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists: testuser");

        // Verify mocks
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verifyNoInteractions(entityMapper);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    // ... (other tests for updateUser, deleteUser, addExperiencePoints, verification methods) ...
    // Ensure any UserDTO instantiations in those tests are also corrected like above.
}