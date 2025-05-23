package com.example.demo.service.unit;

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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpaceMissionServiceTest {

    @Mock
    private SpaceMissionRepository spaceMissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private SpaceMissionServiceImpl spaceMissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateMission_Success() {
        Long creatorUserId = 1L;
        User creator = new User();
        creator.setId(creatorUserId);
        creator.setVerificationStatus(User.UserVerification.VERIFIED);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(null, "Mission Name", "Agency", LocalDateTime.now(),
                "Description", "Image URL", "Stream URL", SpaceMission.MissionStatus.UPCOMING, null, null);

        SpaceMission mission = new SpaceMission();
        mission.setName("Mission Name");

        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creator));
        when(entityMapper.toEntity(missionDTO)).thenReturn(mission);
        when(spaceMissionRepository.save(mission)).thenReturn(mission);
        when(entityMapper.toDTO(mission)).thenReturn(missionDTO);

        SpaceMissionDTO result = spaceMissionService.createMission(missionDTO, creatorUserId);

        assertNotNull(result);
        assertEquals(missionDTO.name(), result.name());
        verify(userRepository, times(1)).findById(creatorUserId);
        verify(spaceMissionRepository, times(1)).save(mission);
    }

    @Test
    void testCreateMission_UserNotVerifiedOrAdmin() {
        Long creatorUserId = 1L;
        User creator = new User();
        creator.setId(creatorUserId);
        creator.setVerificationStatus(User.UserVerification.UNVERIFIED);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(null, "Mission Name", "Agency", LocalDateTime.now(),
                "Description", "Image URL", "Stream URL", SpaceMission.MissionStatus.UPCOMING, null, null);

        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creator));

        assertThrows(VerificationException.class, () -> spaceMissionService.createMission(missionDTO, creatorUserId));
        verify(userRepository, times(1)).findById(creatorUserId);
        verifyNoInteractions(spaceMissionRepository);
    }

    @Test
    void testGetMissionById_Success() {
        Long missionId = 1L;
        SpaceMission mission = new SpaceMission();
        mission.setId(missionId);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(missionId, "Mission Name", "Agency", LocalDateTime.now(),
                "Description", "Image URL", "Stream URL", SpaceMission.MissionStatus.UPCOMING, null, null);

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(entityMapper.toDTO(mission)).thenReturn(missionDTO);

        SpaceMissionDTO result = spaceMissionService.getMissionById(missionId);

        assertNotNull(result);
        assertEquals(missionId, result.id());
        verify(spaceMissionRepository, times(1)).findById(missionId);
    }

    @Test
    void testGetMissionById_NotFound() {
        Long missionId = 1L;

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> spaceMissionService.getMissionById(missionId));
        verify(spaceMissionRepository, times(1)).findById(missionId);
    }

    @Test
    void testGetAllMissions_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        SpaceMission mission = new SpaceMission();
        mission.setId(1L);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(1L, "Mission Name", "Agency", LocalDateTime.now(),
                "Description", "Image URL", "Stream URL", SpaceMission.MissionStatus.UPCOMING, null, null);

        Page<SpaceMission> missions = new PageImpl<>(List.of(mission));
        when(spaceMissionRepository.findAll(pageable)).thenReturn(missions);
        when(entityMapper.toDTO(mission)).thenReturn(missionDTO);

        Page<SpaceMissionDTO> result = spaceMissionService.getAllMissions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(spaceMissionRepository, times(1)).findAll(pageable);
    }

    @Test
    void testUpdateMission_Success() {
        Long missionId = 1L;
        Long userId = 1L;

        SpaceMission existingMission = new SpaceMission();
        existingMission.setId(missionId);
        User creator = new User();
        creator.setId(userId);
        existingMission.setUser(creator);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(missionId, "Updated Name", "Updated Agency", LocalDateTime.now(),
                "Updated Description", "Updated Image URL", "Updated Stream URL", SpaceMission.MissionStatus.IN_PROGRESS, null, null);

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.of(existingMission));
        when(spaceMissionRepository.save(existingMission)).thenReturn(existingMission);
        when(entityMapper.toDTO(existingMission)).thenReturn(missionDTO);

        SpaceMissionDTO result = spaceMissionService.updateMission(missionId, missionDTO, userId);

        assertNotNull(result);
        assertEquals(missionDTO.name(), result.name());
        verify(spaceMissionRepository, times(1)).findById(missionId);
        verify(spaceMissionRepository, times(1)).save(existingMission);
    }

    @Test
    void testUpdateMission_Unauthorized() {
        Long missionId = 1L;
        Long userId = 2L;

        SpaceMission existingMission = new SpaceMission();
        existingMission.setId(missionId);
        User creator = new User();
        creator.setId(1L);
        existingMission.setUser(creator);

        SpaceMissionDTO missionDTO = new SpaceMissionDTO(missionId, "Updated Name", "Updated Agency", LocalDateTime.now(),
                "Updated Description", "Updated Image URL", "Updated Stream URL", SpaceMission.MissionStatus.IN_PROGRESS, null, null);

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.of(existingMission));

        assertThrows(UnauthorizedException.class, () -> spaceMissionService.updateMission(missionId, missionDTO, userId));
        verify(spaceMissionRepository, times(1)).findById(missionId);
        verifyNoMoreInteractions(spaceMissionRepository);
    }

    @Test
    void testDeleteMission_Success() {
        Long missionId = 1L;
        Long userId = 1L;

        SpaceMission mission = new SpaceMission();
        mission.setId(missionId);
        User creator = new User();
        creator.setId(userId);
        mission.setUser(creator);

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));

        spaceMissionService.deleteMission(missionId, userId);

        verify(spaceMissionRepository, times(1)).findById(missionId);
        verify(spaceMissionRepository, times(1)).delete(mission);
    }

    @Test
    void testDeleteMission_Unauthorized() {
        Long missionId = 1L;
        Long userId = 2L;

        SpaceMission mission = new SpaceMission();
        mission.setId(missionId);
        User creator = new User();
        creator.setId(1L);
        mission.setUser(creator);

        when(spaceMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> spaceMissionService.deleteMission(missionId, userId));
        verify(spaceMissionRepository, times(1)).findById(missionId);
        verifyNoMoreInteractions(spaceMissionRepository);
    }
}