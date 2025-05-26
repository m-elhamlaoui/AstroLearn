package com.example.demo.service.integration;

import com.example.demo.dto.LessonDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Course;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.QuizCompletionRepository;
import com.example.demo.service.LessonService;
import com.example.demo.util.TestLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static com.example.demo.util.TestLogger.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ExtendWith(TestLogger.class)

public class LessonServiceIntegrationTest {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizCompletionRepository quizCompletionRepository;

    private Course course;
    private Module module;
    private Lesson lesson1;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for LessonServiceIntegrationTest");
        // Clean up before each test
        quizCompletionRepository.deleteAll(); // Delete quiz completions first
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();

        // Create test course
        course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Course Description");
        course.setDifficulty(Course.DifficultyLevel.BEGINNER);
        course = courseRepository.save(course);

        // Create test module
        module = new Module();
        module.setTitle("Test Module");
        module.setCourse(course);
        module = moduleRepository.save(module);

        // Create test lesson
        lesson1 = new Lesson();
        lesson1.setTitle("Test Lesson 1");
        lesson1.setContent("Content 1");
        lesson1.setModule(module);
        lesson1 = lessonRepository.save(lesson1);
    }

    @Test
    void testAddLessonToModule() {
        LessonDTO newLessonDTO = new LessonDTO(
                null,
                "New Lesson",
                "New Content",
                "http://example.com/video",
                null,
                null,
                0
        );

        LessonDTO createdLesson = lessonService.addLessonToModule(module.getId(), newLessonDTO);

        assertThat(createdLesson).isNotNull();
        assertThat(createdLesson.title()).isEqualTo("New Lesson");
        assertThat(createdLesson.content()).isEqualTo("New Content");
        assertThat(createdLesson.videoUrl()).isEqualTo("http://example.com/video");
        assertThat(createdLesson.moduleId()).isEqualTo(module.getId());

        // Verify in database
        Optional<Lesson> savedLessonOpt = lessonRepository.findById(createdLesson.id());
        assertThat(savedLessonOpt).isPresent();
        assertThat(savedLessonOpt.get().getTitle()).isEqualTo("New Lesson");
        assertThat(savedLessonOpt.get().getModule().getId()).isEqualTo(module.getId());
    }

    @Test
    void testAddLessonToModule_ModuleNotFound() {
        LessonDTO newLessonDTO = new LessonDTO(
                null,
                "New Lesson",
                "New Content",
                "http://example.com/video",
                null,
                null,
                0
        );

        assertThrows(ResourceNotFoundException.class, () ->
                lessonService.addLessonToModule(999L, newLessonDTO));
    }

    @Test
    void testGetLessonById() {
        LessonDTO foundLesson = lessonService.getLessonById(lesson1.getId());

        assertThat(foundLesson).isNotNull();
        assertThat(foundLesson.id()).isEqualTo(lesson1.getId());
        assertThat(foundLesson.title()).isEqualTo("Test Lesson 1");
    }

    @Test
    void testGetLessonById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonById(999L));
    }

    @Test
    void testUpdateLesson() {
        LessonDTO updatedLessonDTO = new LessonDTO(
                lesson1.getId(),
                "Updated Lesson Title",
                "Updated Content",
                "http://example.com/updated_video",
                lesson1.getModule().getId(),
                null,
                0
        );

        LessonDTO result = lessonService.updateLesson(lesson1.getId(), updatedLessonDTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(lesson1.getId());
        assertThat(result.title()).isEqualTo("Updated Lesson Title");
        assertThat(result.content()).isEqualTo("Updated Content");
        assertThat(result.videoUrl()).isEqualTo("http://example.com/updated_video");

        // Verify in database
        Optional<Lesson> updatedLessonOpt = lessonRepository.findById(lesson1.getId());
        assertThat(updatedLessonOpt).isPresent();
        assertThat(updatedLessonOpt.get().getTitle()).isEqualTo("Updated Lesson Title");
        assertThat(updatedLessonOpt.get().getContent()).isEqualTo("Updated Content");
        assertThat(updatedLessonOpt.get().getVideoUrl()).isEqualTo("http://example.com/updated_video");
    }

    @Test
    void testUpdateLesson_NotFound() {
        LessonDTO updatedLessonDTO = new LessonDTO(
                999L,
                "Updated Lesson Title",
                "Updated Content",
                "http://example.com/updated_video",
                module.getId(),
                null,
                0
        );
        assertThrows(ResourceNotFoundException.class, () -> lessonService.updateLesson(999L, updatedLessonDTO));
    }

    @Test
    void testDeleteLesson() {
        Long lessonId = lesson1.getId();

        lessonService.deleteLesson(lessonId);

        // Verify in database
        assertThat(lessonRepository.findById(lessonId)).isEmpty();
    }

    @Test
    void testDeleteLesson_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> lessonService.deleteLesson(999L));
    }

    @Test
    void testGetLessonsByModuleId() {
        // Add another lesson to the module
        Lesson lesson2 = new Lesson();
        lesson2.setTitle("Test Lesson 2");
        lesson2.setContent("Content 2");
        lesson2.setModule(module);
        lessonRepository.save(lesson2);

        // Verify in database
        List<LessonDTO> lessons = lessonService.getLessonsByModuleId(module.getId());

        assertThat(lessons).isNotNull();
        assertThat(lessons).hasSize(2);
        assertThat(lessons.stream().map(LessonDTO::title)).containsExactlyInAnyOrder("Test Lesson 1", "Test Lesson 2");
    }

    @Test
    void testGetLessonsByModuleId_ModuleNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonsByModuleId(999L));
    }
}
