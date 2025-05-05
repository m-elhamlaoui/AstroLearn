package com.example.demo.service;

import com.example.demo.dto.SpaceMissionDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.VerificationException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.SpaceMission;
import com.example.demo.model.User;
import com.example.demo.repository.SpaceMissionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.SpaceMissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceMissionServiceTest {

    @Mock
    private SpaceMissionRepository spaceMissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private SpaceMissionServiceImpl spaceMissionService;

    private User verifiedUser;
    private User adminUser;
    private User unverifiedUser;
    private SpaceMission spaceMission;
    private SpaceMissionDTO spaceMissionDTO;

    @BeforeEach
    void setUp() {
        // Setup verified user
        verifiedUser = new User();
        verifiedUser.setId(1L);
        verifiedUser.setUsername("verifiedUser");
        verifiedUser.setVerificationStatus(User.UserVerification.VERIFIED);
        verifiedUser.setRole(User.UserRole.USER);

        // Setup admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminUser");
        adminUser.setVerificationStatus(User.UserVerification.VERIFIED);
        adminUser.setRole(User.UserRole.ADMIN);

        // Setup unverified user
        unverifiedUser = new User();
        unverifiedUser.setId(3L);
        unverifiedUser.setUsername("unverifiedUser");
        unverifiedUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        unverifiedUser.setRole(User.UserRole.USER);

        // Setup space mission
        spaceMission = new SpaceMission();
        spaceMission.setId(1L);
        spaceMission.setName("Test Mission");
        spaceMission.setAgency("Test Agency");
        spaceMission.setLaunchDate(LocalDateTime.now().plusDays(30));
        spaceMission.setDescription("Test Description");
        spaceMission.setMissionImage("test-image.jpg");
        spaceMission.setLiveStreamUrl("https://test-stream.com");
        spaceMission.setStatus(SpaceMission.MissionStatus.UPCOMING);
        spaceMission.setUser(verifiedUser);

        // Setup space mission DTO
        spaceMissionDTO = new SpaceMissionDTO(
                1L,
                "Test Mission",
                "Test Agency",
                LocalDateTime.now().plusDays(30),
                "Test Description",
                "test-image.jpg",
                "https://test-stream.com",
                SpaceMission.MissionStatus.UPCOMING,
                1L,
                "verifiedUser"
        );
    }

    @Test
    @DisplayName("Test createMission with verified user - Success")
    void givenVerifiedUser_whenCreateMission_thenReturnMissionDTO() {
        // Arrange
        SpaceMissionDTO createMissionDTO = new SpaceMissionDTO(
                null,
                "New Mission",
                "New Agency",
                LocalDateTime.now().plusDays(60),
                "New Description",
                "new-image.jpg",
                "https://new-stream.com",
                null,
                1L,
                "verifiedUser"
        );

        SpaceMission missionToSave = new SpaceMission();
        missionToSave.setName("New Mission");
        missionToSave.setAgency("New Agency");
        missionToSave.setLaunchDate(LocalDateTime.now().plusDays(60));
        missionToSave.setDescription("New Description");
        missionToSave.setMissionImage("new-image.jpg");
        missionToSave.setLiveStreamUrl("https://new-stream.com");
        missionToSave.setStatus(SpaceMission.MissionStatus.UPCOMING);
        missionToSave.setUser(verifiedUser);

        SpaceMission savedMission = new SpaceMission();
        savedMission.setId(2L);
        savedMission.setName("New Mission");
        savedMission.setAgency("New Agency");
        savedMission.setLaunchDate(LocalDateTime.now().plusDays(60));
        savedMission.setDescription("New Description");
        savedMission.setMissionImage("new-image.jpg");
        savedMission.setLiveStreamUrl("https://new-stream.com");
        savedMission.setStatus(SpaceMission.MissionStatus.UPCOMING);
        savedMission.setUser(verifiedUser);

        SpaceMissionDTO expectedSavedMissionDTO = new SpaceMissionDTO(
                2L,
                "New Mission",
                "New Agency",
                LocalDateTime.now().plusDays(60),
                "New Description",
                "new-image.jpg",
                "https://new-stream.com",
                SpaceMission.MissionStatus.UPCOMING,
                1L,
                "verifiedUser"
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(verifiedUser));
        given(entityMapper.toEntity(createMissionDTO)).willReturn(missionToSave);
        given(spaceMissionRepository.save(any(SpaceMission.class))).willReturn(savedMission);
        given(entityMapper.toDTO(savedMission)).willReturn(expectedSavedMissionDTO);

        // Act
        SpaceMissionDTO resultDTO = spaceMissionService.createMission(createMissionDTO, 1L);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(2L);
        assertThat(resultDTO.name()).isEqualTo("New Mission");
        assertThat(resultDTO.agency()).isEqualTo("New Agency");
        assertThat(resultDTO.status()).isEqualTo(SpaceMission.MissionStatus.UPCOMING);
        assertThat(resultDTO.creatorUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(entityMapper).toEntity(createMissionDTO);
        verify(spaceMissionRepository).save(any(SpaceMission.class));
        verify(entityMapper).toDTO(savedMission);
    }

    @Test
    @DisplayName("Test createMission with admin user - Success")
    void givenAdminUser_whenCreateMission_thenReturnMissionDTO() {
        // Arrange
        SpaceMissionDTO createMissionDTO = new SpaceMissionDTO(
                null,
                "Admin Mission",
                "Admin Agency",
                LocalDateTime.now().plusDays(90),
                "Admin Description",
                "admin-image.jpg",
                "https://admin-stream.com",
                null,
                2L,
                "adminUser"
        );

        SpaceMission missionToSave = new SpaceMission();
        missionToSave.setName("Admin Mission");
        missionToSave.setAgency("Admin Agency");
        missionToSave.setLaunchDate(LocalDateTime.now().plusDays(90));
        missionToSave.setDescription("Admin Description");
        missionToSave.setMissionImage("admin-image.jpg");
        missionToSave.setLiveStreamUrl("https://admin-stream.com");
        missionToSave.setStatus(SpaceMission.MissionStatus.UPCOMING);
        missionToSave.setUser(adminUser);

        SpaceMission savedMission = new SpaceMission();
        savedMission.setId(3L);
        savedMission.setName("Admin Mission");
        savedMission.setAgency("Admin Agency");
        savedMission.setLaunchDate(LocalDateTime.now().plusDays(90));
        savedMission.setDescription("Admin Description");
        savedMission.setMissionImage("admin-image.jpg");
        savedMission.setLiveStreamUrl("https://admin-stream.com");
        savedMission.setStatus(SpaceMission.MissionStatus.UPCOMING);
        savedMission.setUser(adminUser);

        SpaceMissionDTO expectedSavedMissionDTO = new SpaceMissionDTO(
                3L,
                "Admin Mission",
                "Admin Agency",
                LocalDateTime.now().plusDays(90),
                "Admin Description",
                "admin-image.jpg",
                "https://admin-stream.com",
                SpaceMission.MissionStatus.UPCOMING,
                2L,
                "adminUser"
        );

        given(userRepository.findById(2L)).willReturn(Optional.of(adminUser));
        given(entityMapper.toEntity(createMissionDTO)).willReturn(missionToSave);
        given(spaceMissionRepository.save(any(SpaceMission.class))).willReturn(savedMission);
        given(entityMapper.toDTO(savedMission)).willReturn(expectedSavedMissionDTO);

        // Act
        SpaceMissionDTO resultDTO = spaceMissionService.createMission(createMissionDTO, 2L);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(3L);
        assertThat(resultDTO.name()).isEqualTo("Admin Mission");
        assertThat(resultDTO.agency()).isEqualTo("Admin Agency");
        assertThat(resultDTO.status()).isEqualTo(SpaceMission.MissionStatus.UPCOMING);
        assertThat(resultDTO.creatorUserId()).isEqualTo(2L);

        verify(userRepository).findById(2L);
        verify(entityMapper).toEntity(createMissionDTO);
        verify(spaceMissionRepository).save(any(SpaceMission.class));
        verify(entityMapper).toDTO(savedMission);
    }

    @Test
    @DisplayName("Test createMission with unverified user - Failure")
    void givenUnverifiedUser_whenCreateMission_thenThrowsVerificationException() {
        // Arrange
        SpaceMissionDTO createMissionDTO = new SpaceMissionDTO(
                null,
                "Unverified Mission",
                "Unverified Agency",
                LocalDateTime.now().plusDays(120),
                "Unverified Description",
                "unverified-image.jpg",
                "https://unverified-stream.com",
                null,
                3L,
                "unverifiedUser"
        );

        given(userRepository.findById(3L)).willReturn(Optional.of(unverifiedUser));

        // Act & Assert
        assertThatThrownBy(() -> spaceMissionService.createMission(createMissionDTO, 3L))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("User must be verified or an admin to create missions");

        verify(userRepository).findById(3L);
        verifyNoInteractions(entityMapper);
        verifyNoInteractions(spaceMissionRepository);
    }

    @Test
    @DisplayName("Test getMissionById - Success")
    void givenMissionId_whenGetMissionById_thenReturnMissionDTO() {
        // Arrange
        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));
        given(entityMapper.toDTO(spaceMission)).willReturn(spaceMissionDTO);

        // Act
        SpaceMissionDTO resultDTO = spaceMissionService.getMissionById(1L);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(1L);
        assertThat(resultDTO.name()).isEqualTo("Test Mission");
        assertThat(resultDTO.agency()).isEqualTo("Test Agency");
        assertThat(resultDTO.status()).isEqualTo(SpaceMission.MissionStatus.UPCOMING);
        assertThat(resultDTO.creatorUserId()).isEqualTo(1L);

        verify(spaceMissionRepository).findById(1L);
        verify(entityMapper).toDTO(spaceMission);
    }

    @Test
    @DisplayName("Test getMissionById - Not Found")
    void givenNonExistentMissionId_whenGetMissionById_thenThrowsResourceNotFoundException() {
        // Arrange
        given(spaceMissionRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> spaceMissionService.getMissionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SpaceMission not found with id : '99'");

        verify(spaceMissionRepository).findById(99L);
        verifyNoInteractions(entityMapper);
    }

    @Test
    @DisplayName("Test getAllMissions - Success")
    void whenGetAllMissions_thenReturnListOfMissionDTOs() {
        // Arrange
        List<SpaceMission> missions = Arrays.asList(spaceMission);
        List<SpaceMissionDTO> expectedDTOs = Arrays.asList(spaceMissionDTO);

        given(spaceMissionRepository.findAll()).willReturn(missions);
        given(entityMapper.toDTO(spaceMission)).willReturn(spaceMissionDTO);

        // Act
        List<SpaceMissionDTO> resultDTOs = spaceMissionService.getAllMissions();

        // Assert
        assertThat(resultDTOs).isNotNull();
        assertThat(resultDTOs).hasSize(1);
        assertThat(resultDTOs.get(0).id()).isEqualTo(1L);
        assertThat(resultDTOs.get(0).name()).isEqualTo("Test Mission");
        assertThat(resultDTOs.get(0).agency()).isEqualTo("Test Agency");
        assertThat(resultDTOs.get(0).status()).isEqualTo(SpaceMission.MissionStatus.UPCOMING);
        assertThat(resultDTOs.get(0).creatorUserId()).isEqualTo(1L);

        verify(spaceMissionRepository).findAll();
        verify(entityMapper).toDTO(spaceMission);
    }

    @Test
    @DisplayName("Test updateMission with creator user - Success")
    void givenCreatorUser_whenUpdateMission_thenReturnUpdatedMissionDTO() {
        // Arrange
        SpaceMissionDTO updateMissionDTO = new SpaceMissionDTO(
                1L,
                "Updated Mission",
                "Updated Agency",
                LocalDateTime.now().plusDays(45),
                "Updated Description",
                "updated-image.jpg",
                "https://updated-stream.com",
                SpaceMission.MissionStatus.IN_PROGRESS,
                1L,
                "verifiedUser"
        );

        SpaceMission updatedMission = new SpaceMission();
        updatedMission.setId(1L);
        updatedMission.setName("Updated Mission");
        updatedMission.setAgency("Updated Agency");
        updatedMission.setLaunchDate(LocalDateTime.now().plusDays(45));
        updatedMission.setDescription("Updated Description");
        updatedMission.setMissionImage("updated-image.jpg");
        updatedMission.setLiveStreamUrl("https://updated-stream.com");
        updatedMission.setStatus(SpaceMission.MissionStatus.IN_PROGRESS);
        updatedMission.setUser(verifiedUser);

        SpaceMissionDTO expectedUpdatedMissionDTO = new SpaceMissionDTO(
                1L,
                "Updated Mission",
                "Updated Agency",
                LocalDateTime.now().plusDays(45),
                "Updated Description",
                "updated-image.jpg",
                "https://updated-stream.com",
                SpaceMission.MissionStatus.IN_PROGRESS,
                1L,
                "verifiedUser"
        );

        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));
        given(spaceMissionRepository.save(any(SpaceMission.class))).willReturn(updatedMission);
        given(entityMapper.toDTO(updatedMission)).willReturn(expectedUpdatedMissionDTO);

        // Act
        SpaceMissionDTO resultDTO = spaceMissionService.updateMission(1L, updateMissionDTO, 1L);

        // Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(1L);
        assertThat(resultDTO.name()).isEqualTo("Updated Mission");
        assertThat(resultDTO.agency()).isEqualTo("Updated Agency");
        assertThat(resultDTO.status()).isEqualTo(SpaceMission.MissionStatus.IN_PROGRESS);
        assertThat(resultDTO.creatorUserId()).isEqualTo(1L);

        verify(spaceMissionRepository).findById(1L);
        verify(spaceMissionRepository).save(any(SpaceMission.class));
        verify(entityMapper).toDTO(updatedMission);
    }

    @Test
    @DisplayName("Test updateMission with non-creator user - Failure")
    void givenNonCreatorUser_whenUpdateMission_thenThrowsUnauthorizedException() {
        // Arrange
        SpaceMissionDTO updateMissionDTO = new SpaceMissionDTO(
                1L,
                "Updated Mission",
                "Updated Agency",
                LocalDateTime.now().plusDays(45),
                "Updated Description",
                "updated-image.jpg",
                "https://updated-stream.com",
                SpaceMission.MissionStatus.IN_PROGRESS,
                1L,
                "verifiedUser"
        );

        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));

        // Act & Assert
        assertThatThrownBy(() -> spaceMissionService.updateMission(1L, updateMissionDTO, 3L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User is not the creator and cannot update this mission");

        verify(spaceMissionRepository).findById(1L);
        verifyNoInteractions(entityMapper);
        verify(spaceMissionRepository, never()).save(any(SpaceMission.class));
    }

    @Test
    @DisplayName("Test deleteMission with creator user - Success")
    void givenCreatorUser_whenDeleteMission_thenSuccess() {
        // Arrange
        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));
        given(userRepository.findById(1L)).willReturn(Optional.of(verifiedUser));

        // Act
        spaceMissionService.deleteMission(1L, 1L);

        // Assert
        verify(spaceMissionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(spaceMissionRepository).delete(spaceMission);
    }

    @Test
    @DisplayName("Test deleteMission with admin user - Success")
    void givenAdminUser_whenDeleteMission_thenSuccess() {
        // Arrange
        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));
        given(userRepository.findById(2L)).willReturn(Optional.of(adminUser));

        // Act
        spaceMissionService.deleteMission(1L, 2L);

        // Assert
        verify(spaceMissionRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(spaceMissionRepository).delete(spaceMission);
    }

    @Test
    @DisplayName("Test deleteMission with non-creator and non-admin user - Failure")
    void givenNonCreatorAndNonAdminUser_whenDeleteMission_thenThrowsUnauthorizedException() {
        // Arrange
        given(spaceMissionRepository.findById(1L)).willReturn(Optional.of(spaceMission));
        given(userRepository.findById(3L)).willReturn(Optional.of(unverifiedUser));

        // Act & Assert
        assertThatThrownBy(() -> spaceMissionService.deleteMission(1L, 3L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User is not the creator or an admin and cannot delete this mission");

        verify(spaceMissionRepository).findById(1L);
        verify(userRepository).findById(3L);
        verify(spaceMissionRepository, never()).delete(any(SpaceMission.class));
    }
}
