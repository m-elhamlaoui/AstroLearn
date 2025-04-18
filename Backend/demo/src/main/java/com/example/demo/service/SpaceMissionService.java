// src/main/java/com/example/demo/service/SpaceMissionService.java
package com.example.demo.service;

import com.example.demo.dto.SpaceMissionDTO;
import java.util.List;

public interface SpaceMissionService {

    /*
     * Creates a new space mission. Requires the user to be VERIFIED or ADMIN.
     * @param missionDTO Mission data
     * @param creatorUserId ID of the user creating the mission
     * @return Created SpaceMissionDTO
     * @throws ResourceNotFoundException if user not found
     * @throws VerificationException if user is not verified or admin
     */
    SpaceMissionDTO createMission(SpaceMissionDTO missionDTO, Long creatorUserId);

    /*
     * Finds a mission by its ID.
     * @param id Mission ID
     * @return SpaceMissionDTO
     * @throws ResourceNotFoundException if mission not found
     */
    SpaceMissionDTO getMissionById(Long id);


    List<SpaceMissionDTO> getAllMissions();

    /*
     * Updates an existing space mission. Only creator should be allowed.
     * @param id Mission ID
     * @param missionDTO Updated data
     * @param userId ID of user attempting update (for permission check)
     * @return Updated SpaceMissionDTO
     * @throws ResourceNotFoundException if mission not found
     * @throws UnauthorizedException if user is not creator or admin
     */
    SpaceMissionDTO updateMission(Long id, SpaceMissionDTO missionDTO, Long userId);

    /*
     * Deletes a space mission. Only creator should be allowed.
     * @param id Mission ID
     * @param userId ID of user attempting delete (for permission check)
     * @throws ResourceNotFoundException if mission not found
     * @throws UnauthorizedException if user is not creator or admin
     */
    void deleteMission(Long id, Long userId);
}