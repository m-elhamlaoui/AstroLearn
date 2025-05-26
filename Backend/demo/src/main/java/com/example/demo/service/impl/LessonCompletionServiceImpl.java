package com.example.demo.service.impl;

import com.example.demo.dto.CourseProgressSummaryDTO;
import com.example.demo.dto.LessonCompletionDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.LessonCompletionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonCompletionServiceImpl implements LessonCompletionService {
    
    private static final Logger logger = LoggerFactory.getLogger(LessonCompletionServiceImpl.class);
    
    private final LessonCompletionRepository lessonCompletionRepository;
    private final UserCurrentLessonRepository userCurrentLessonRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    
    @Override
    public LessonCompletionDTO markLessonCompleted(Long userId, Long lessonId, Long courseId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        // Fetch required entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        
        // Verify that the lesson belongs to the specified course
        if (lesson.getModule() == null || lesson.getModule().getCourse() == null || 
                !lesson.getModule().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Lesson " + lessonId + " does not belong to course " + courseId);
        }
        
        // Check if the lesson is already completed
        Optional<LessonCompletion> existingCompletion = 
                lessonCompletionRepository.findByUserIdAndLessonId(userId, lessonId);
        
        if (existingCompletion.isPresent()) {
            // Lesson already completed, return the existing completion
            LessonCompletion completion = existingCompletion.get();
            return mapToDTO(completion);
        }
        
        // Create a new completion record
        LessonCompletion completion = new LessonCompletion();
        completion.setUser(user);
        completion.setLesson(lesson);
        completion.setCourse(course);
        completion.setCompletionDate(LocalDateTime.now());
        
        LessonCompletion savedCompletion = lessonCompletionRepository.save(completion);
        
        // Also update the current lesson for the user in this course
        updateCurrentLesson(user, course, lesson);
        
        return mapToDTO(savedCompletion);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonCompletionDTO> getUserCompletions(Long userId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        return lessonCompletionRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LessonCompletionDTO> getUserCourseCompletions(Long userId, Long courseId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        // Verify user and course exist
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        return lessonCompletionRepository.findByUserIdAndCourseId(userId, courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isLessonCompleted(Long userId, Long lessonId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        return lessonCompletionRepository.existsByUserIdAndLessonId(userId, lessonId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseProgressSummaryDTO getCourseProgressSummary(Long userId, Long courseId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        // Fetch user and course
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        // Get total lessons in the course
        int totalLessons = lessonRepository.countByModuleCourseId(courseId);
        
        // Get completed lessons
        List<Long> completedLessonIds = 
                lessonCompletionRepository.findCompletedLessonIdsByUserIdAndCourseId(userId, courseId);
        int completedLessons = completedLessonIds.size();
        
        // Calculate completion percentage
        double completionPercentage = totalLessons > 0 
                ? ((double) completedLessons / totalLessons) * 100 
                : 0.0;
        
        // Determine if course is completed (100% of lessons completed)
        boolean completed = completionPercentage >= 100.0;
        
        // Get current lesson
        Long currentLessonId = null;
        Optional<UserCurrentLesson> currentLessonOpt = 
                userCurrentLessonRepository.findByUserIdAndCourseId(userId, courseId);
        
        if (currentLessonOpt.isPresent()) {
            currentLessonId = currentLessonOpt.get().getLesson().getId();
        }
        
        return new CourseProgressSummaryDTO(
                courseId,
                course.getTitle(),
                userId,
                user.getUsername(),
                totalLessons,
                completedLessons,
                completionPercentage,
                completed,
                currentLessonId,
                completedLessonIds
        );
    }
    
    @Override
    public CourseProgressSummaryDTO setCurrentLesson(Long userId, Long courseId, Long lessonId) {
        // Security check: Ensure user is authenticated
        validateUserAccess();
        
        // Fetch required entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        
        // Verify that the lesson belongs to the specified course
        if (lesson.getModule() == null || lesson.getModule().getCourse() == null || 
                !lesson.getModule().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Lesson " + lessonId + " does not belong to course " + courseId);
        }
        
        // Update the current lesson
        updateCurrentLesson(user, course, lesson);
        
        // Return the updated progress summary
        return getCourseProgressSummary(userId, courseId);
    }
    
    /**
     * Helper method to update the current lesson for a user in a course
     */
    private void updateCurrentLesson(User user, Course course, Lesson lesson) {
        Optional<UserCurrentLesson> currentLessonOpt = 
                userCurrentLessonRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        
        UserCurrentLesson currentLesson;
        
        if (currentLessonOpt.isPresent()) {
            // Update existing record
            currentLesson = currentLessonOpt.get();
            currentLesson.setLesson(lesson);
        } else {
            // Create new record
            currentLesson = new UserCurrentLesson();
            currentLesson.setUser(user);
            currentLesson.setCourse(course);
            currentLesson.setLesson(lesson);
        }
        
        currentLesson.setLastAccessed(LocalDateTime.now());
        userCurrentLessonRepository.save(currentLesson);
    }
    
    /**
     * Helper method to map a LessonCompletion entity to a LessonCompletionDTO
     */
    private LessonCompletionDTO mapToDTO(LessonCompletion completion) {
        return new LessonCompletionDTO(
                completion.getId(),
                completion.getUser().getId(),
                completion.getUser().getUsername(),
                completion.getLesson().getId(),
                completion.getLesson().getTitle(),
                completion.getCourse().getId(),
                completion.getCourse().getTitle(),
                completion.getCompletionDate()
        );
    }
    
    /**
     * Validates that the current user is authenticated.
     * Access is granted to any authenticated user, regardless of their role.
     *
     * @throws UnauthorizedException if the user is not authenticated
     */
    private void validateUserAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        // No further validation needed - any authenticated user can access course progress
        logger.debug("Course progress access granted for authenticated user");
    }
}
