package com.example.demo.service.impl;

import com.example.demo.dto.SpaceMissionDTO;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.SpaceMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceMissionServiceImpl implements SpaceMissionService {

    private final SpaceMissionRepository spaceMissionRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Override
    public SpaceMissionDTO createMission(SpaceMissionDTO missionDTO, Long creatorUserId) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        // TODO: Add security check: User must be VERIFIED or ADMIN
        if (creator.getVerificationStatus() != User.UserVerification.VERIFIED && creator.getRole() != User.UserRole.ADMIN) {
            System.out.println("Placeholder: Security check needed - User " + creatorUserId + " must be VERIFIED or ADMIN to create mission");
            throw new VerificationException("User must be verified or an admin to create missions."); // Keep exception for now
        }

        SpaceMission mission = entityMapper.toEntity(missionDTO);
        mission.setUser(creator);
        if (mission.getStatus() == null) {
            mission.setStatus(SpaceMission.MissionStatus.UPCOMING);
        }

        SpaceMission savedMission = spaceMissionRepository.save(mission);
        return entityMapper.toDTO(savedMission);
    }

    @Override
    @Transactional(readOnly = true)
    public SpaceMissionDTO getMissionById(Long id) {
        SpaceMission mission = spaceMissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpaceMission", "id", id));
        return entityMapper.toDTO(mission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpaceMissionDTO> getAllMissions(Pageable pageable) {
        return spaceMissionRepository.findAll(pageable)
                .map(entityMapper::toDTO);
    }

    @Override
    public SpaceMissionDTO updateMission(Long id, SpaceMissionDTO missionDTO, Long userId) {
        SpaceMission existingMission = spaceMissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpaceMission", "id", id));

        //Add security check: Ensure userId == existingMission creator
        System.out.println("Placeholder: Security check needed for user " + userId + " updating mission " + id);
        if (existingMission.getUser() == null || !existingMission.getUser().getId().equals(userId)) {
            // Temporary enforcement
            throw new UnauthorizedException("User is not the creator and cannot update this mission.");
        }

        // Update fields - Check if EntityMapper.update exists or map manually
        existingMission.setName(missionDTO.name());
        existingMission.setAgency(missionDTO.agency());
        existingMission.setLaunchDate(missionDTO.launchDate());
        existingMission.setDescription(missionDTO.description());
        existingMission.setMissionImage(missionDTO.missionImage());
        existingMission.setLiveStreamUrl(missionDTO.liveStreamUrl());
        existingMission.setStatus(missionDTO.status());

        SpaceMission updatedMission = spaceMissionRepository.save(existingMission);
        return entityMapper.toDTO(updatedMission);
    }

    @Override
    public void deleteMission(Long id, Long userId) {
        SpaceMission mission = spaceMissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpaceMission", "id", id));

        // Add security check: Ensure userId == mission creator OR userId is ADMIN
        User user = userRepository.findById(userId).orElse(null); // Need user to check role
        boolean isCreator = mission.getUser() != null && mission.getUser().getId().equals(userId);
        boolean isAdmin = user != null && user.getRole() == User.UserRole.ADMIN;

        System.out.println("Placeholder: Security check needed for user " + userId + " deleting mission " + id);

        if (!isCreator && !isAdmin) {
            // Temporary enforcement
            throw new UnauthorizedException("User is not the creator or an admin and cannot delete this mission.");
        }

        spaceMissionRepository.delete(mission);
    }

    @Override
    public Page<SpaceMissionDTO> getMissionsByStatus(String status, Pageable pageable) {
        SpaceMission.MissionStatus missionStatus;
        try {
            missionStatus = SpaceMission.MissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid mission status: " + status);
        }

        return spaceMissionRepository.findByStatus(missionStatus, pageable)
                .map(mission -> entityMapper.toDTO((SpaceMission) mission));
    }
    @Override
    @Transactional(readOnly = true)
    public List<SpaceMissionDTO> getMissionsByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);

        List<SpaceMission> missions = spaceMissionRepository.findByLaunchDateBetween(start, end);
        return missions.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}