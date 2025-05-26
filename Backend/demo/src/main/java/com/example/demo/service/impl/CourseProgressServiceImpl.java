package com.example.demo.service.impl;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.CourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseProgressServiceImpl implements CourseProgressService {
    
    // Role constant
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EntityMapper entityMapper;

    @Override
    public CourseProgressDTO getOrCreateCourseProgress(Long userId, Long courseId) {
        // Security check: Ensure requesting user ID matches userId or user is admin
        validateUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Optional<CourseProgress> existingProgressOpt = courseProgressRepository.findByUserIdAndCourseId(userId, courseId);

        CourseProgress progress;
        if (existingProgressOpt.isPresent()) {
            progress = existingProgressOpt.get();
        } else {
            progress = new CourseProgress();
            progress.setUser(user);
            progress.setCourse(course);
            progress.setCompleted(false);
            progress.setCompletionPercentage(0.0);
        }

        progress.setLastAccessed(LocalDateTime.now());
        CourseProgress savedProgress = courseProgressRepository.save(progress);
        return entityMapper.toDTO(savedProgress);
    }

    @Override
    public CourseProgressDTO markLessonCompleted(Long userId, Long courseId, Long lessonId) {
        // Security check: Ensure requesting user ID matches userId or user is admin
        validateUserAccess(userId);

        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseProgress for user " + userId + " and course " + courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (lesson.getModule() == null || lesson.getModule().getCourse() == null || !lesson.getModule().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Lesson " + lessonId + " does not belong to course " + courseId);
        }

        // Create a mutable copy of the completedLessonIds set
        Set<Long> completedLessonIds = new HashSet<>(progress.getCompletedLessonIds());
        boolean added = completedLessonIds.add(lessonId);

        if (added) {
            progress.setCompletedLessonIds(completedLessonIds); // Update the mutable set back to progress
            progress.setLastAccessed(LocalDateTime.now());
            
            // Calculate completion percentage directly
            int totalLessons = lessonRepository.countByModuleCourseId(courseId);
            progress.setCompletionPercentage(((double) completedLessonIds.size() / totalLessons) * 100);
            progress.setCompleted(progress.getCompletionPercentage() >= 100);
            
            progress = courseProgressRepository.save(progress);
        } else {
            progress.setLastAccessed(LocalDateTime.now()); // Update last accessed regardless
        }

        return entityMapper.toDTO(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getProgressByUserId(Long userId) {
        // Security check: Ensure requesting user ID matches userId or user is admin
        validateUserAccess(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return courseProgressRepository.findByUserId(userId).stream() // Assumes findByUserId exists
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressDTO getProgressByUserAndCourse(Long userId, Long courseId) {
        // Security check: Ensure requesting user ID matches userId or user is admin
        validateUserAccess(userId);

        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseProgress for user " + userId + " and course " + courseId));
        return entityMapper.toDTO(progress);
    }

    @Override
    public CourseProgressDTO setCurrentLesson(Long userId, Long courseId, Long lessonId) {
        // Security check: Ensure requesting user ID matches userId or user is admin
        validateUserAccess(userId);

        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseProgress for user " + userId + " and course " + courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (lesson.getModule() == null || lesson.getModule().getCourse() == null || !lesson.getModule().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Lesson " + lessonId + " does not belong to course " + courseId);
        }

        progress.setCurrentLesson(lesson);
        progress.setLastAccessed(LocalDateTime.now());
        CourseProgress savedProgress = courseProgressRepository.save(progress);
        return entityMapper.toDTO(savedProgress);
    }
    
    /**
     * Validates that the current authenticated user has access to the specified user's data.
     * Access is granted if the authenticated user ID matches the requested user ID or if the user has ADMIN role.
     *
     * @param userId The ID of the user whose data is being accessed
     * @throws UnauthorizedException if the current user doesn't have access
     */
    private void validateUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new UnauthorizedException("Invalid authentication");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN));
        boolean isResourceOwner = userDetails.getId().equals(userId);
        
        if (!isAdmin && !isResourceOwner) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
    }
}
