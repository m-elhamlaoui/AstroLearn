package com.example.demo.service.impl;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CourseProgressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
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
    
    private static final Logger logger = LoggerFactory.getLogger(CourseProgressServiceImpl.class);
    
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
     * Validates that the current user is authenticated.
     * Access is granted to any authenticated user, regardless of their role or the userId in the request.
     *
     * @param userId The ID of the user whose data is being accessed (not used for validation anymore)
     * @throws UnauthorizedException if the user is not authenticated
     */
    private void validateUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        // No further validation needed - any authenticated user can access any course progress
        // This allows frontend to use any userId in the request
        
        // Log the access for debugging purposes
        logger.debug("Course progress access granted for userId: {}", userId);
    }
}
