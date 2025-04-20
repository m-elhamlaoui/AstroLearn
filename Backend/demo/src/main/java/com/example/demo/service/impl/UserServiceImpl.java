package com.example.demo.service.impl;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor // Constructor injection via Lombok
@Transactional // Make methods transactional by default
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return entityMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return entityMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return entityMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // 1. Check for existing username/email
        if (userRepository.existsByUsername(userDTO.username())) {
            throw new BadRequestException("Username already exists: " + userDTO.username());
        }
        if (userRepository.existsByEmail(userDTO.email())) {
            throw new BadRequestException("Email already exists: " + userDTO.email());
        }

        // 2. Map DTO to Entity
        User user = entityMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.password()));

        // add the other field existing in the user model without commenting them
        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setBio(userDTO.bio());
        user.setProfileImageUrl(userDTO.profileImageUrl());
        user.setPhotoCoverUrl(userDTO.photoCoverUrl());


        // 3. Set Defaults (redundant if defaults are set in entity, but safe)
        user.setRole(User.UserRole.USER); // Default role
        user.setVerificationStatus(User.UserVerification.UNVERIFIED);
        user.setLevel(User.UserLevel.NOVICE);
        user.setExperiencePoints(0);


        // 4. Save User
        User savedUser = userRepository.save(user);

        // 5. Map back to DTO (ensure password is not included)
        return entityMapper.toDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // --- Security Check ---
        // In a real app, ensure the logged-in user is the one being updated or an admin.
        // Example: checkAuthentication(id);

        // Use the mapper to update only non-null fields from DTO onto the existing entity
        entityMapper.updateUserFromDto(userDTO, existingUser);

        // 4. Handle special fields
        if (userDTO.password() != null && !userDTO.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.password()));
        }
        if (userDTO.email() != null && !userDTO.email().equals(existingUser.getEmail())) {
            existingUser.setEmail(userDTO.email());
        }

        User updatedUser = userRepository.save(existingUser);
        return entityMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // --- Security Check ---
        // In a real app, ensure the logged-in user is the one being deleted or an admin deleting his account not others.

        userRepository.delete(user); // Cascading deletion defined in User entity handles related data
    }


    @Override
    public void addExperiencePoints(Long userId, int points) {
        if (points <= 0) return; // Ignore non-positive points

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.addExperience(points); // This method in User entity handles level update
        userRepository.save(user);
    }

    // --- Verification Implementation ---

    @Override
    public void requestVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getVerificationStatus() == User.UserVerification.VERIFIED) {
            throw new VerificationException("User is already verified.");
        }
        if (user.getVerificationStatus() == User.UserVerification.PENDING) {
            throw new VerificationException("Verification request is already pending.");
        }

        user.setVerificationStatus(User.UserVerification.PENDING);
        userRepository.save(user);
        // TODO: Optionally notify admins about the new request
    }

    @Override
    public void approveVerification(Long adminUserId, Long targetUserId) {
        User admin = findUserAndCheckAdmin(adminUserId);
        User targetUser = findUserAndCheckPending(targetUserId);

        targetUser.setVerificationStatus(User.UserVerification.VERIFIED);
        userRepository.save(targetUser);
        // TODO: Optionally notify the target user about approval
    }

    @Override
    public void rejectVerification(Long adminUserId, Long targetUserId) {
        User admin = findUserAndCheckAdmin(adminUserId);
        User targetUser = findUserAndCheckPending(targetUserId);

        targetUser.setVerificationStatus(User.UserVerification.UNVERIFIED); // Or a specific REJECTED status if needed
        userRepository.save(targetUser);
        // TODO: Optionally notify the target user about rejection (maybe include reason)
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByVerificationStatus(User.UserVerification status) {

         return userRepository.findByVerificationStatus(status).stream()
              .map(entityMapper::toDTO)
              .collect(Collectors.toList());
    }


    // --- Helper methods for verification ---
    private User findUserAndCheckAdmin(Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin User", "id", adminUserId));
        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new UnauthorizedException("User does not have ADMIN privileges.");
        }
        return admin;
    }

    private User findUserAndCheckPending(Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target User", "id", targetUserId));
        if (targetUser.getVerificationStatus() != User.UserVerification.PENDING) {
            throw new VerificationException("User verification status is not PENDING.");
        }
        return targetUser;
    }

    // --- Helper method for Security (Placeholder) ---
    private void checkAuthentication(Long resourceOwnerId) {
        // TODO: Implement security check using Spring Security
        // Get authenticated user principal
        // Compare principal's ID with resourceOwnerId
        // Check if principal has ADMIN role
        // Throw UnauthorizedException if access denied
        System.out.println("Placeholder: Security check needed for user ID: " + resourceOwnerId);
    }
}
