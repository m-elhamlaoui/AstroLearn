package com.example.demo.service.integration;

import com.example.demo.dto.CourseProgressDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Course;
import com.example.demo.model.CourseProgress;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.model.User;
import com.example.demo.repository.CourseProgressRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.QuizCompletionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CourseProgressService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class CourseProgressServiceIntegrationTest {

    @Autowired
    private CourseProgressService courseProgressService;

    @Autowired
    private CourseProgressRepository courseProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuizCompletionRepository quizCompletionRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Course course;
    private Module module;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        // Clean up before each test - order matters due to foreign key constraints
        quizCompletionRepository.deleteAll(); // Delete quiz completions first
        courseProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test user with different data from seeder
        user = new User();
        user.setUsername("course_learner");
        user.setEmail("learner@space.com");
        user.setPassword("learnspace123");
        user.setRole(User.UserRole.USER);
        user.setVerificationStatus(User.UserVerification.VERIFIED);
        user.setBio("Passionate about learning space science");
        user.setProfileImageUrl("https://i.pravatar.cc/150?u=learner");
        user.setPhotoCoverUrl("https://picsum.photos/seed/learnercover/800/200");
        user.setExperiencePoints(1500);
        user = userRepository.save(user);
        entityManager.flush();

        // Create test course with different data from seeder
        course = new Course();
        course.setTitle("Advanced Space Propulsion Systems");
        course.setDescription("Deep dive into cutting-edge propulsion technologies for space exploration");
        course.setDifficulty(Course.DifficultyLevel.ADVANCED);
        course.setImageUrl("https://picsum.photos/seed/propulsion/600/300");
        course = courseRepository.save(course);
        entityManager.flush();

        // Create test module with different data from seeder
        module = new Module();
        module.setTitle("Nuclear Propulsion Technologies");
        module.setCourse(course);
        module = moduleRepository.save(module);
        entityManager.flush();

        // Create test lessons with different data from seeder
        lesson1 = new Lesson();
        lesson1.setTitle("Nuclear Thermal Propulsion Basics");
        lesson1.setContent("Understanding the principles of nuclear thermal propulsion and its advantages");
        lesson1.setVideoUrl("https://www.youtube.com/watch?v=nuclear-propulsion");
        lesson1.setModule(module);
        lesson1 = lessonRepository.save(lesson1);
        entityManager.flush();

        lesson2 = new Lesson();
        lesson2.setTitle("Nuclear Electric Propulsion Systems");
        lesson2.setContent("Exploring ion thrusters and other electric propulsion technologies");
        lesson2.setVideoUrl("https://www.youtube.com/watch?v=electric-propulsion");
        lesson2.setModule(module);
        lesson2 = lessonRepository.save(lesson2);
        entityManager.flush();

        // Link lessons to module
        module.getLessons().add(lesson1);
        module.getLessons().add(lesson2);
        module = moduleRepository.save(module);
        entityManager.flush();

        // Link module to course
        course.getModules().add(module);
        course = courseRepository.save(course);
        entityManager.flush();

        // Refresh the course to ensure totalLessons is calculated
        entityManager.refresh(course);
        entityManager.clear();
    }

    @Test
    void testGetOrCreateCourseProgress_CreateNew() {
        CourseProgressDTO progress = courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());

        assertThat(progress).isNotNull();
        assertThat(progress.userId()).isEqualTo(user.getId());
        assertThat(progress.courseId()).isEqualTo(course.getId());
        assertThat(progress.completed()).isFalse();
        assertThat(progress.completionPercentage()).isEqualTo(0.0);
        assertThat(progress.completedLessonIds()).isEmpty();
        assertThat(progress.currentLessonId()).isNull();
        assertThat(progress.lastAccessed()).isNotNull();

        // Verify in database
        Optional<CourseProgress> savedProgressOpt = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        assertThat(savedProgressOpt).isPresent();
        assertThat(savedProgressOpt.get().getCompletedLessonIds()).isEmpty();
    }

    @Test
    void testGetOrCreateCourseProgress_GetExisting() {
        // Create existing progress
        CourseProgress existingProgress = new CourseProgress();
        existingProgress.setUser(user);
        existingProgress.setCourse(course);
        existingProgress.setCompleted(false);
        existingProgress.setCompletionPercentage(0.0);
        existingProgress.setLastAccessed(LocalDateTime.now().minusDays(1));
        courseProgressRepository.save(existingProgress);
        entityManager.flush();
        entityManager.clear();

        CourseProgressDTO progress = courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());

        assertThat(progress).isNotNull();
        assertThat(progress.id()).isEqualTo(existingProgress.getId());
        assertThat(progress.userId()).isEqualTo(user.getId());
        assertThat(progress.courseId()).isEqualTo(course.getId());
        assertThat(progress.lastAccessed()).isAfter(existingProgress.getLastAccessed()); // Last accessed should be updated
    }

    @Test
    void testMarkLessonCompleted() {
        // Create initial progress
        CourseProgressDTO initialProgress = courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());
        entityManager.flush();
        entityManager.clear();

        // Mark lesson1 as completed
        CourseProgressDTO progress1 = courseProgressService.markLessonCompleted(user.getId(), course.getId(), lesson1.getId());

        assertThat(progress1).isNotNull();
        assertThat(progress1.completedLessonIds()).containsExactly(lesson1.getId());
        // Assuming 2 lessons total, completion should be 50%
        assertThat(progress1.completionPercentage()).isEqualTo(50.0);
        assertThat(progress1.completed()).isFalse();

        // Mark lesson2 as completed
        CourseProgressDTO progress2 = courseProgressService.markLessonCompleted(user.getId(), course.getId(), lesson2.getId());

        assertThat(progress2).isNotNull();
        assertThat(progress2.completedLessonIds()).containsExactlyInAnyOrder(lesson1.getId(), lesson2.getId());
        // Assuming 2 lessons total, completion should be 100%
        assertThat(progress2.completionPercentage()).isEqualTo(100.0);
        assertThat(progress2.completed()).isTrue();

        // Mark lesson1 again (should not change anything)
        CourseProgressDTO progress3 = courseProgressService.markLessonCompleted(user.getId(), course.getId(), lesson1.getId());
        assertThat(progress3.completedLessonIds()).containsExactlyInAnyOrder(lesson1.getId(), lesson2.getId());
        assertThat(progress3.completionPercentage()).isEqualTo(100.0);
        assertThat(progress3.completed()).isTrue();
    }

    @Test
    void testMarkLessonCompleted_LessonNotInCourse() {
        // Create initial progress
        courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());
        entityManager.flush();
        entityManager.clear();

        // Create a lesson not in this course
        final Lesson outsideLesson = new Lesson(); // Declare as final
        outsideLesson.setTitle("Outside Lesson");
        outsideLesson.setContent("Content");
        // No module or a module from a different course
        lessonRepository.save(outsideLesson); // Save the lesson

        assertThrows(BadRequestException.class, () ->
                courseProgressService.markLessonCompleted(user.getId(), course.getId(), outsideLesson.getId()));
    }

    @Test
    void testGetProgressByUserId() {
        // Create progress for the user in two courses
        Course course2 = new Course();
        course2.setTitle("Test Course 2");
        course2.setDescription("Desc 2");
        course2.setDifficulty(Course.DifficultyLevel.INTERMEDIATE);
        course2 = courseRepository.save(course2);

        CourseProgress progress1 = new CourseProgress();
        progress1.setUser(user);
        progress1.setCourse(course);
        progress1.setCompleted(false);
        progress1.setCompletionPercentage(0.0);
        progress1.setLastAccessed(LocalDateTime.now());
        courseProgressRepository.save(progress1);

        CourseProgress progress2 = new CourseProgress();
        progress2.setUser(user);
        progress2.setCourse(course2);
        progress2.setCompleted(false);
        progress2.setCompletionPercentage(0.0);
        progress2.setLastAccessed(LocalDateTime.now());
        courseProgressRepository.save(progress2);

        entityManager.flush();
        entityManager.clear();

        List<CourseProgressDTO> userProgressList = courseProgressService.getProgressByUserId(user.getId());

        assertThat(userProgressList).isNotNull();
        assertThat(userProgressList).hasSize(2);
        assertThat(userProgressList.stream().map(CourseProgressDTO::courseId))
                .containsExactlyInAnyOrder(course.getId(), course2.getId());
    }

    @Test
    void testGetProgressByUserAndCourse() {
        // Create progress for the user in the course
        CourseProgress existingProgress = new CourseProgress();
        existingProgress.setUser(user);
        existingProgress.setCourse(course);
        existingProgress.setCompleted(false);
        existingProgress.setCompletionPercentage(0.0);
        existingProgress.setLastAccessed(LocalDateTime.now());
        courseProgressRepository.save(existingProgress);
        entityManager.flush();
        entityManager.clear();

        CourseProgressDTO progress = courseProgressService.getProgressByUserAndCourse(user.getId(), course.getId());

        assertThat(progress).isNotNull();
        assertThat(progress.id()).isEqualTo(existingProgress.getId());
        assertThat(progress.userId()).isEqualTo(user.getId());
        assertThat(progress.courseId()).isEqualTo(course.getId());
    }

    @Test
    void testGetProgressByUserAndCourse_NotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                courseProgressService.getProgressByUserAndCourse(user.getId(), 999L));
        assertThrows(ResourceNotFoundException.class, () ->
                courseProgressService.getProgressByUserAndCourse(999L, course.getId()));
    }

    @Test
    void testSetCurrentLesson() {
        // Create initial progress
        courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());
        entityManager.flush();
        entityManager.clear();

        // Set current lesson to lesson1
        CourseProgressDTO progress1 = courseProgressService.setCurrentLesson(user.getId(), course.getId(), lesson1.getId());

        assertThat(progress1).isNotNull();
        assertThat(progress1.currentLessonId()).isEqualTo(lesson1.getId());
        assertThat(progress1.lastAccessed()).isNotNull();

        // Verify in database
        Optional<CourseProgress> savedProgressOpt1 = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        assertThat(savedProgressOpt1).isPresent();
        assertThat(savedProgressOpt1.get().getCurrentLesson().getId()).isEqualTo(lesson1.getId());

        // Set current lesson to lesson2
        CourseProgressDTO progress2 = courseProgressService.setCurrentLesson(user.getId(), course.getId(), lesson2.getId());

        assertThat(progress2).isNotNull();
        assertThat(progress2.currentLessonId()).isEqualTo(lesson2.getId());
        assertThat(progress2.lastAccessed()).isAfter(progress1.lastAccessed()); // Last accessed should be updated

        // Verify in database
        Optional<CourseProgress> savedProgressOpt2 = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        assertThat(savedProgressOpt2).isPresent();
        assertThat(savedProgressOpt2.get().getCurrentLesson().getId()).isEqualTo(lesson2.getId());
    }

    @Test
    void testSetCurrentLesson_LessonNotInCourse() {
        // Create initial progress
        courseProgressService.getOrCreateCourseProgress(user.getId(), course.getId());
        entityManager.flush();
        entityManager.clear();

        // Create a lesson not in this course
        Lesson tempOutsideLesson = new Lesson();
        tempOutsideLesson.setTitle("Outside Lesson");
        tempOutsideLesson.setContent("Content");
        final Lesson outsideLesson = lessonRepository.save(tempOutsideLesson); // Declared as final after saving

        assertThrows(BadRequestException.class, () ->
                courseProgressService.setCurrentLesson(user.getId(), course.getId(), outsideLesson.getId()));
    }
}
