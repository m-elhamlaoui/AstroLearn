package com.example.demo.integration;

import com.example.demo.dto.CourseDTO;
import com.example.demo.model.Course;
import com.example.demo.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        setUpTestUser();
    }

    @Test
    void shouldCreateAndRetrieveCourse() {
        // Given
        CourseDTO courseDTO = new CourseDTO(
            null, // id will be generated
            "Introduction to Astronomy",
            "A comprehensive introduction to the basics of astronomy",
            Course.DifficultyLevel.BEGINNER,
            0,
            new ArrayList<>()
        );

        // When
        CourseDTO savedCourse = courseService.createCourse(courseDTO);

        // Then
        assertThat(savedCourse.id()).isNotNull();
        CourseDTO foundCourse = courseService.getCourseById(savedCourse.id());
        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.title()).isEqualTo("Introduction to Astronomy");
        assertThat(foundCourse.difficulty()).isEqualTo(Course.DifficultyLevel.BEGINNER);
    }

    @Test
    void shouldListAllCourses() {
        // Given
        CourseDTO course1 = new CourseDTO(
            null,
            "Astronomy Basics",
            "Learn the fundamentals of astronomy",
            Course.DifficultyLevel.BEGINNER,
            0,
            new ArrayList<>()
        );

        CourseDTO course2 = new CourseDTO(
            null,
            "Advanced Astrophysics",
            "Deep dive into astrophysics concepts",
            Course.DifficultyLevel.ADVANCED,
            0,
            new ArrayList<>()
        );

        courseService.createCourse(course1);
        courseService.createCourse(course2);

        // When
        List<CourseDTO> courses = courseService.getAllCourses();

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(CourseDTO::title)
                         .containsExactlyInAnyOrder("Astronomy Basics", "Advanced Astrophysics");
    }

    @Test
    void shouldUpdateCourse() {
        // Given
        CourseDTO originalCourse = new CourseDTO(
            null,
            "Original Course",
            "Original description",
            Course.DifficultyLevel.BEGINNER,
            0,
            new ArrayList<>()
        );

        CourseDTO savedCourse = courseService.createCourse(originalCourse);

        // When
        CourseDTO updatedCourseDTO = new CourseDTO(
            savedCourse.id(),
            "Updated Course",
            "Updated description",
            Course.DifficultyLevel.INTERMEDIATE,
            savedCourse.totalLessons(),
            savedCourse.moduleIds()
        );

        CourseDTO updatedCourse = courseService.updateCourse(savedCourse.id(), updatedCourseDTO);

        // Then
        assertThat(updatedCourse.title()).isEqualTo("Updated Course");
        assertThat(updatedCourse.difficulty()).isEqualTo(Course.DifficultyLevel.INTERMEDIATE);
    }

    @Test
    void shouldDeleteCourse() {
        // Given
        CourseDTO course = new CourseDTO(
            null,
            "Course to Delete",
            "This course will be deleted",
            Course.DifficultyLevel.BEGINNER,
            0,
            new ArrayList<>()
        );

        CourseDTO savedCourse = courseService.createCourse(course);

        // When
        courseService.deleteCourse(savedCourse.id());

        // Then
        List<CourseDTO> courses = courseService.getAllCourses();
        assertThat(courses).extracting(CourseDTO::id)
                         .doesNotContain(savedCourse.id());
    }
} 