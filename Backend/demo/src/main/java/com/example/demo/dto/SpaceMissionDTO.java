package com.example.demo.dto;

import com.example.demo.model.SpaceMission;
import java.time.LocalDateTime;

public record SpaceMissionDTO(
        Long id,
        String name,
        String agency,
        LocalDateTime launchDate,
        String description,
        String missionImage,
        String liveStreamUrl,
        SpaceMission.MissionStatus status,
        Long creatorUserId, // ID of the User who created the mission
        String creatorUsername // Optional convenience field
) {}
