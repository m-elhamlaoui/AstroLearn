package com.example.demo.dto;

import com.example.demo.model.User;

public record UserDTO(
        Long id,
        String username,
        String email,
        String profileImageUrl,
        User.UserRole role,
        int experiencePoints,
        User.UserLevel level
) {}

