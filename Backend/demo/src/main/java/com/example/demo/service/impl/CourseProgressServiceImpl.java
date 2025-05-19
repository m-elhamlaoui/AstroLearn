package com.example.demo.service.impl;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CourseProgressService;
import lombok.RequiredArgsConstructor;
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

    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EntityMapper entityMapper;

    @Override
    public CourseProgressDTO getOrCreateCourseProgress(Long userId, Long courseId) {
        // TODO: Add security check: Ensure requesting user ID matches userId

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
    // TODO: Add security check: Ensure requesting user ID matches userId

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
        // @PreUpdate in CourseProgress entity should recalculate completion %
        progress = courseProgressRepository.save(progress);
    } else {
        progress.setLastAccessed(LocalDateTime.now()); // Update last accessed regardless
    }

    return entityMapper.toDTO(progress);
}
    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getProgressByUserId(Long userId) {
        // TODO: Add security check: Ensure requesting user ID matches userId OR user is ADMIN

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
        // TODO: Add security check: Ensure requesting user ID matches userId OR user is ADMIN

        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseProgress for user " + userId + " and course " + courseId));
        return entityMapper.toDTO(progress);
    }

    @Override
    public CourseProgressDTO setCurrentLesson(Long userId, Long courseId, Long lessonId) {
        // TODO: Add security check: Ensure requesting user ID matches userId

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
}
