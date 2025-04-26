package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User; // Keep using model enums here
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    //  @throws BadRequestException if username or email already exists
    UserDTO createUser(UserDTO userDTO);


    //  @throws ResourceNotFoundException if user not found

    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    UserDTO getUserByEmail(String email);
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);      // Handles cascading deletion based on entity relationships (orphanRemoval=true).
    // Method to search users
    Page<UserDTO> searchUsersByUsername(String searchTerm, Pageable pageable);

    // Adds experience points to a user and potentially updates their level.
    void addExperiencePoints(Long userId, int points);


    // --- Verification Specific Methods ---
    void requestVerification(Long userId);


     // @throws UnauthorizedException if the adminUserId does not belong to an ADMIN.
     // @throws VerificationException if the target user is not in PENDING state.
    void approveVerification(Long targetUserId);
    void rejectVerification( Long targetUserId, String reason);

    List<UserDTO> getUsersByVerificationStatus(User.UserVerification status);

}