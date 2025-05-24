package com.example.demo.service.integration;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Course;
import com.example.demo.model.Module;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.QuizCompletionRepository;
import com.example.demo.service.ModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.util.BaseIntegrationTest;
import static com.example.demo.util.TestLogger.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModuleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizCompletionRepository quizCompletionRepository;

    private Course course;
    private Module module1;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for ModuleServiceIntegrationTest");
        // Clean up before each test
        quizCompletionRepository.deleteAll(); // Delete quiz completions first
        moduleRepository.deleteAll();
        courseRepository.deleteAll();

        // Create test course
        course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Course Description");
        course.setDifficulty(Course.DifficultyLevel.BEGINNER);
        course = courseRepository.save(course);

        // Create test module
        module1 = new Module();
        module1.setTitle("Test Module 1");
        module1.setCourse(course);
        module1 = moduleRepository.save(module1);
    }

    @Test
    void testAddModuleToCourse() {
        ModuleDTO newModuleDTO = new ModuleDTO(
                null,
                "New Module",
                null,
                0,
                null
        );

        ModuleDTO createdModule = moduleService.addModuleToCourse(course.getId(), newModuleDTO);

        assertThat(createdModule).isNotNull();
        assertThat(createdModule.title()).isEqualTo("New Module");
        assertThat(createdModule.courseId()).isEqualTo(course.getId());

        // Verify in database
        Optional<Module> savedModuleOpt = moduleRepository.findById(createdModule.id());
        assertThat(savedModuleOpt).isPresent();
        assertThat(savedModuleOpt.get().getTitle()).isEqualTo("New Module");
        assertThat(savedModuleOpt.get().getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void testAddModuleToCourse_CourseNotFound() {
        ModuleDTO newModuleDTO = new ModuleDTO(
                null,
                "New Module",
                null,
                0,
                null
        );

        assertThrows(ResourceNotFoundException.class, () ->
                moduleService.addModuleToCourse(999L, newModuleDTO));
    }

    @Test
    void testGetModuleById() {
        ModuleDTO foundModule = moduleService.getModuleById(module1.getId());

        assertThat(foundModule).isNotNull();
        assertThat(foundModule.id()).isEqualTo(module1.getId());
        assertThat(foundModule.title()).isEqualTo("Test Module 1");
    }

    @Test
    void testGetModuleById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> moduleService.getModuleById(999L));
    }

    @Test
    void testUpdateModule() {
        ModuleDTO updatedModuleDTO = new ModuleDTO(
                module1.getId(),
                "Updated Module Title",
                module1.getCourse().getId(),
                0,
                null
        );

        ModuleDTO result = moduleService.updateModule(module1.getId(), updatedModuleDTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(module1.getId());
        assertThat(result.title()).isEqualTo("Updated Module Title");

        // Verify in database
        Optional<Module> updatedModuleOpt = moduleRepository.findById(module1.getId());
        assertThat(updatedModuleOpt).isPresent();
        assertThat(updatedModuleOpt.get().getTitle()).isEqualTo("Updated Module Title");
    }

    @Test
    void testUpdateModule_NotFound() {
        ModuleDTO updatedModuleDTO = new ModuleDTO(
                999L,
                "Updated Module Title",
                course.getId(),
                0,
                null
        );
        assertThrows(ResourceNotFoundException.class, () -> moduleService.updateModule(999L, updatedModuleDTO));
    }

    @Test
    void testDeleteModule() {
        Long moduleId = module1.getId();
        moduleService.deleteModule(moduleId);

        // Verify in database
        assertThat(moduleRepository.findById(moduleId)).isEmpty();
    }

    @Test
    void testDeleteModule_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> moduleService.deleteModule(999L));
    }

    @Test
    void testGetModulesByCourseId() {
        // Add another module to the course
        Module module2 = new Module();
        module2.setTitle("Test Module 2");
        module2.setCourse(course);
        moduleRepository.save(module2);

        // Verify in database
        List<ModuleDTO> modules = moduleService.getModulesByCourseId(course.getId());

        assertThat(modules).isNotNull();
        assertThat(modules).hasSize(2);
        assertThat(modules.stream().map(ModuleDTO::title)).containsExactlyInAnyOrder("Test Module 1", "Test Module 2");
    }

    @Test
    void testGetModulesByCourseId_CourseNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> moduleService.getModulesByCourseId(999L));
    }
}
