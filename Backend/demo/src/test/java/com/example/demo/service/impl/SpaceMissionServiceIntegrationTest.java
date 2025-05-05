package com.example.demo.integration;

import com.example.demo.dto.SpaceMissionDTO;
import com.example.demo.model.SpaceMission;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SpaceMissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SpaceMissionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SpaceMissionService spaceMissionService;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        setUpTestUser();
    }

    @Test
    void shouldCreateAndRetrieveMission() {
        // Given
        LocalDateTime launchDate = LocalDateTime.now();
        SpaceMissionDTO missionDTO = new SpaceMissionDTO(
            null, // id will be generated
            "Test Mission",
            "NASA",
            launchDate,
            "Test Description",
            "mission-image.jpg",
            "http://livestream.com",
            SpaceMission.MissionStatus.UPCOMING,
            testUserId, // use the created test user
            "testuser"
        );

        // When
        SpaceMissionDTO savedMission = spaceMissionService.createMission(missionDTO, testUserId);

        // Then
        assertThat(savedMission.id()).isNotNull();
        SpaceMissionDTO foundMission = spaceMissionService.getMissionById(savedMission.id());
        assertThat(foundMission).isNotNull();
        assertThat(foundMission.name()).isEqualTo("Test Mission");
    }

    @Test
    void shouldListAllMissions() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        SpaceMissionDTO mission1 = new SpaceMissionDTO(
            null,
            "Mission 1",
            "NASA",
            now,
            "Description 1",
            "mission1-image.jpg",
            "http://livestream1.com",
            SpaceMission.MissionStatus.UPCOMING,
            testUserId,
            "testuser"
        );

        SpaceMissionDTO mission2 = new SpaceMissionDTO(
            null,
            "Mission 2",
            "ESA",
            now.plusDays(1),
            "Description 2",
            "mission2-image.jpg",
            "http://livestream2.com",
            SpaceMission.MissionStatus.UPCOMING,
            testUserId,
            "testuser"
        );

        spaceMissionService.createMission(mission1, testUserId);
        spaceMissionService.createMission(mission2, testUserId);

        // When
        List<SpaceMissionDTO> missions = spaceMissionService.getAllMissions();

        // Then
        assertThat(missions).hasSize(2);
        assertThat(missions).extracting(SpaceMissionDTO::name)
                          .containsExactlyInAnyOrder("Mission 1", "Mission 2");
    }

    @Test
    void shouldUpdateMission() {
        // Given
        LocalDateTime launchDate = LocalDateTime.now();
        SpaceMissionDTO originalMission = new SpaceMissionDTO(
            null,
            "Original Mission",
            "NASA",
            launchDate,
            "Original Description",
            "original-image.jpg",
            "http://original-livestream.com",
            SpaceMission.MissionStatus.UPCOMING,
            testUserId,
            "testuser"
        );

        SpaceMissionDTO savedMission = spaceMissionService.createMission(originalMission, testUserId);

        // When
        SpaceMissionDTO updatedMissionDTO = new SpaceMissionDTO(
            savedMission.id(),
            "Updated Mission",
            savedMission.agency(),
            savedMission.launchDate(),
            savedMission.description(),
            savedMission.missionImage(),
            savedMission.liveStreamUrl(),
            SpaceMission.MissionStatus.IN_PROGRESS,
            savedMission.creatorUserId(),
            savedMission.creatorUsername()
        );

        SpaceMissionDTO updatedMission = spaceMissionService.updateMission(savedMission.id(), updatedMissionDTO, testUserId);

        // Then
        assertThat(updatedMission.name()).isEqualTo("Updated Mission");
        assertThat(updatedMission.status()).isEqualTo(SpaceMission.MissionStatus.IN_PROGRESS);
    }
} 