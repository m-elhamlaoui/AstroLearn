package com.example.demo.controller;

import com.example.demo.dto.SpaceMissionDTO;
import com.example.demo.service.SpaceMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class SpaceMissionController {

    private final SpaceMissionService spaceMissionService;

    // Create a new mission
    @PostMapping
    public ResponseEntity<SpaceMissionDTO> createMission(@RequestBody SpaceMissionDTO missionDTO, @RequestParam Long creatorUserId) {
        SpaceMissionDTO createdMission = spaceMissionService.createMission(missionDTO, creatorUserId);
        return ResponseEntity.ok(createdMission);
    }

    // Get a mission by ID
    @GetMapping("/{id}")
    public ResponseEntity<SpaceMissionDTO> getMissionById(@PathVariable Long id) {
        SpaceMissionDTO mission = spaceMissionService.getMissionById(id);
        return ResponseEntity.ok(mission);
    }

    // Get all missions
    @GetMapping
    public ResponseEntity<Page<SpaceMissionDTO>> getAllMissions(Pageable pageable) {
        Page<SpaceMissionDTO> missions = spaceMissionService.getAllMissions(pageable);
        return ResponseEntity.ok(missions);
    }

    // Update a mission
    @PutMapping("/{id}")
    public ResponseEntity<SpaceMissionDTO> updateMission(@PathVariable Long id, @RequestBody SpaceMissionDTO missionDTO, @RequestParam Long userId) {
        SpaceMissionDTO updatedMission = spaceMissionService.updateMission(id, missionDTO, userId);
        return ResponseEntity.ok(updatedMission);
    }

    // Delete a mission
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id, @RequestParam Long userId) {
        spaceMissionService.deleteMission(id, userId);
        return ResponseEntity.noContent().build();
    }

    // add the controller to get method by status
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<SpaceMissionDTO>> getMissionsByStatus(@PathVariable String status, Pageable pageable) {
        Page<SpaceMissionDTO> missions = spaceMissionService.getMissionsByStatus(status, pageable);
        return ResponseEntity.ok(missions);
    }


    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<SpaceMissionDTO>> getMissionsByMonth(@PathVariable int year, @PathVariable int month) {
        List<SpaceMissionDTO> missions = spaceMissionService.getMissionsByMonth(year, month);
        return ResponseEntity.ok(missions);
    }
}