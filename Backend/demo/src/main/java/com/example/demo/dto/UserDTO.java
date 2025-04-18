package com.example.demo.dto;

import com.example.demo.model.User;

import java.util.List;

// Represents public or semi-public User information
public record UserDTO(
        Long id,
        String username,
        String email,
        String bio,
        String password,
        String profileImageUrl,
        String photoCoverUrl,
        User.UserRole role,
        User.UserVerification verificationStatus,
        User.UserLevel level,
        int experiencePoints,
        // --- Calculated/Aggregated Data (Optional, depends on use case) ---
        Long articleCount,
        Long commentCount,
        Long quizCompletionCount,
        // --- Relationships (Collections represented minimally) ---
        // Avoid listing all IDs if they can be very large lists.
        // Provide dedicated endpoints like /users/{id}/articles, /users/{id}/course-progress etc.
        // List<Long> articleIds,          // IDs of Articles written by the user
        // List<Long> commentIds,          // IDs of Comments written by the user
        // List<Long> articleRatingIds,    // IDs of Ratings given by the user
        List<Long> readingHistoryIds,   // IDs of ReadingHistory records
        List<Long> courseProgressIds,   // IDs of CourseProgress records
        List<Long> quizCompletionIds,   // IDs of QuizCompletions
        List<Long> createdSpaceMissionIds // IDs of SpaceMissions created by the user
) {}

