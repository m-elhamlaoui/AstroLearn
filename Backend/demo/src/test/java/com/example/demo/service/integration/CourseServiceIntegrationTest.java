package com.example.demo.service.integration;

import com.example.demo.dto.CourseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.QuizCompletionRepository;
import com.example.demo.service.CourseService;
import com.example.demo.util.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.example.demo.util.TestLogger.*;

public class CourseServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizCompletionRepository quizCompletionRepository;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for CourseServiceIntegrationTest");
        // Clean up before each test
        quizCompletionRepository.deleteAll(); // Delete quiz completions first
        courseRepository.deleteAll();
        logStep("Database cleaned up");

        // Create test courses
        course1 = new Course();
        course1.setTitle("Test Course 1");
        course1.setDescription("Description 1");
        course1.setDifficulty(Course.DifficultyLevel.BEGINNER);
        course1 = courseRepository.save(course1);

        course2 = new Course();
        course2.setTitle("Test Course 2");
        course2.setDescription("Description 2");
        course2.setDifficulty(Course.DifficultyLevel.INTERMEDIATE);
        course2 = courseRepository.save(course2);
    }

    @Test
    void testCreateCourse() {
        CourseDTO newCourseDTO = new CourseDTO(
                null,
                "New Course",
                "New Description",
                null, // imageUrl
                Course.DifficultyLevel.ADVANCED,
                0, // totalLessons
                null // moduleIds
        );

        CourseDTO createdCourse = courseService.createCourse(newCourseDTO);

        assertThat(createdCourse).isNotNull();
        assertThat(createdCourse.title()).isEqualTo("New Course");
        assertThat(createdCourse.description()).isEqualTo("New Description");
        assertThat(createdCourse.difficulty()).isEqualTo(Course.DifficultyLevel.ADVANCED);

        // Verify in database
        Optional<Course> savedCourseOpt = courseRepository.findById(createdCourse.id());
        assertThat(savedCourseOpt).isPresent();
        assertThat(savedCourseOpt.get().getTitle()).isEqualTo("New Course");
    }

    @Test
    void testGetCourseById() {
        CourseDTO foundCourse = courseService.getCourseById(course1.getId());

        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.id()).isEqualTo(course1.getId());
        assertThat(foundCourse.title()).isEqualTo("Test Course 1");
    }

    @Test
    void testGetCourseById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(999L));
    }

    @Test
    void testGetAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();

        assertThat(courses).isNotNull();
        assertThat(courses).hasSize(2);
        assertThat(courses.stream().map(CourseDTO::title)).containsExactlyInAnyOrder("Test Course 1", "Test Course 2");
    }

    @Test
    void testUpdateCourse() {
        CourseDTO updatedCourseDTO = new CourseDTO(
                course1.getId(),
                "Updated Course Title",
                "Updated Description",
                null, // imageUrl
                Course.DifficultyLevel.ADVANCED,
                0, // totalLessons
                null // moduleIds
        );

        CourseDTO result = courseService.updateCourse(course1.getId(), updatedCourseDTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(course1.getId());
        assertThat(result.title()).isEqualTo("Updated Course Title");
        assertThat(result.description()).isEqualTo("Updated Description");
        assertThat(result.difficulty()).isEqualTo(Course.DifficultyLevel.ADVANCED);

        // Verify in database
        Optional<Course> updatedCourseOpt = courseRepository.findById(course1.getId());
        assertThat(updatedCourseOpt).isPresent();
        assertThat(updatedCourseOpt.get().getTitle()).isEqualTo("Updated Course Title");
        assertThat(updatedCourseOpt.get().getDescription()).isEqualTo("Updated Description");
        assertThat(updatedCourseOpt.get().getDifficulty()).isEqualTo(Course.DifficultyLevel.ADVANCED);
    }

    @Test
    void testUpdateCourse_NotFound() {
        CourseDTO updatedCourseDTO = new CourseDTO(
                999L,
                "Updated Course Title",
                "Updated Description",
                null, // imageUrl
                Course.DifficultyLevel.ADVANCED,
                0, // totalLessons
                null // moduleIds
        );
        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(999L, updatedCourseDTO));
    }

    @Test
    void testDeleteCourse() {
        Long courseId = course1.getId();
        courseService.deleteCourse(courseId);

        // Verify in database
        assertThat(courseRepository.findById(courseId)).isEmpty();
    }

    @Test
    void testDeleteCourse_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(999L));
    }
}
