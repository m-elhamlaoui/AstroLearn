package com.example.demo.service.integration;

import com.example.demo.dto.SpaceMissionDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.SpaceMission;
import com.example.demo.model.User;
import com.example.demo.repository.SpaceMissionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SpaceMissionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.util.BaseIntegrationTest;
import static com.example.demo.util.TestLogger.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpaceMissionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SpaceMissionService spaceMissionService;

    @Autowired
    private SpaceMissionRepository spaceMissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private SpaceMissionDTO testMissionDTO;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for SpaceMissionServiceIntegrationTest");
        // Clean up before each test - order matters due to foreign key constraints
        spaceMissionRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test user with different data from seeder
        testUser = new User();
        testUser.setUsername("mission_creator");
        testUser.setEmail("mission@space.com");
        testUser.setPassword("spacepass123");
        testUser.setRole(User.UserRole.USER);
        testUser.setVerificationStatus(User.UserVerification.VERIFIED);
        testUser.setBio("Space mission enthusiast and creator");
        testUser.setProfileImageUrl("https://i.pravatar.cc/150?u=mission");
        testUser.setPhotoCoverUrl("https://picsum.photos/seed/missioncover/800/200");
        testUser.setExperiencePoints(2000);
        testUser = userRepository.save(testUser);
        entityManager.flush();

        // Create test mission DTO with different data from seeder
        testMissionDTO = new SpaceMissionDTO(
                null, // id
                "Lunar Gateway Construction", // name
                "NASA/ESA/JAXA", // agency
                LocalDateTime.now().plusMonths(3), // launchDate
                "International space station in lunar orbit to support Artemis missions", // description
                "https://picsum.photos/seed/lunargateway/600/400", // missionImage
                "https://www.nasa.gov/live", // liveStreamUrl
                SpaceMission.MissionStatus.UPCOMING, // status
                testUser.getId(), // creatorUserId
                testUser.getUsername() // creatorUsername
        );
    }

    @Test
    void testCreateMission() {
        SpaceMissionDTO createdMission = spaceMissionService.createMission(testMissionDTO, testUser.getId());
        assertThat(createdMission).isNotNull();
        assertThat(createdMission.name()).isEqualTo("Lunar Gateway Construction");
        assertThat(createdMission.agency()).isEqualTo("NASA/ESA/JAXA");
    }

    @Test
    void testGetMissionById() {
        SpaceMissionDTO createdMission = spaceMissionService.createMission(testMissionDTO, testUser.getId());
        SpaceMissionDTO foundMission = spaceMissionService.getMissionById(createdMission.id());
        assertThat(foundMission).isNotNull();
        assertThat(foundMission.id()).isEqualTo(createdMission.id());
        assertThat(foundMission.name()).isEqualTo("Lunar Gateway Construction");
    }

    @Test
    void testGetMissionById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> spaceMissionService.getMissionById(999L));
    }

    @Test
    void testUpdateMission() {
        SpaceMissionDTO createdMission = spaceMissionService.createMission(testMissionDTO, testUser.getId());
        SpaceMissionDTO updatedMissionDTO = new SpaceMissionDTO(
                createdMission.id(), // id
                "Updated Mission", // name
                "Updated Agency", // agency
                LocalDateTime.now().plusDays(2), // launchDate
                "Updated Description", // description
                "http://example.com/updated-mission.jpg", // missionImage
                "http://example.com/updated-livestream", // liveStreamUrl
                SpaceMission.MissionStatus.UPCOMING, // status
                testUser.getId(), // creatorUserId
                testUser.getUsername() // creatorUsername
        );
        SpaceMissionDTO updatedMission = spaceMissionService.updateMission(createdMission.id(), updatedMissionDTO, testUser.getId());
        assertThat(updatedMission).isNotNull();
        assertThat(updatedMission.name()).isEqualTo("Updated Mission");
        assertThat(updatedMission.agency()).isEqualTo("Updated Agency");
    }

    @Test
    void testUpdateMission_NotFound() {
        SpaceMissionDTO updatedMissionDTO = new SpaceMissionDTO(
                999L, // id
                "Updated Mission", // name
                "Updated Agency", // agency
                LocalDateTime.now().plusDays(2), // launchDate
                "Updated Description", // description
                "http://example.com/updated-mission.jpg", // missionImage
                "http://example.com/updated-livestream", // liveStreamUrl
                SpaceMission.MissionStatus.UPCOMING, // status
                testUser.getId(), // creatorUserId
                testUser.getUsername() // creatorUsername
        );
        assertThrows(ResourceNotFoundException.class, () -> spaceMissionService.updateMission(999L, updatedMissionDTO, testUser.getId()));
    }

    @Test
    void testDeleteMission() {
        SpaceMissionDTO createdMission = spaceMissionService.createMission(testMissionDTO, testUser.getId());
        spaceMissionService.deleteMission(createdMission.id(), testUser.getId());
        assertThrows(ResourceNotFoundException.class, () -> spaceMissionService.getMissionById(createdMission.id()));
    }

    @Test
    void testDeleteMission_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> spaceMissionService.deleteMission(999L, testUser.getId()));
    }
} 
