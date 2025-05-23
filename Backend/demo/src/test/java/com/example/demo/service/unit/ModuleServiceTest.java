package com.example.demo.service.unit;

    import com.example.demo.dto.ModuleDTO;
    import com.example.demo.exception.ResourceNotFoundException;
    import com.example.demo.mapper.EntityMapper;
    import com.example.demo.model.Course;
    import com.example.demo.model.Module;
    import com.example.demo.repository.CourseRepository;
    import com.example.demo.repository.ModuleRepository;
    import com.example.demo.service.impl.ModuleServiceImpl;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.MockitoAnnotations;

    import java.util.Collections;
    import java.util.List;
    import java.util.Optional;

    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;

    class ModuleServiceTest {

        @Mock
        private CourseRepository courseRepository;

        @Mock
        private ModuleRepository moduleRepository;

        @Mock
        private EntityMapper entityMapper;

        @InjectMocks
        private ModuleServiceImpl moduleService;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        void testAddModuleToCourse_Success() {
            // Arrange
            Long courseId = 1L;
            ModuleDTO moduleDTO = new ModuleDTO(null, "Test Module", null, 0, Collections.emptyList());

            Course course = new Course();
            course.setId(courseId);

            Module module = new Module();
            module.setTitle(moduleDTO.title());
            module.setLessons(Collections.emptyList());

            Module savedModule = new Module();
            savedModule.setId(1L);
            savedModule.setTitle(moduleDTO.title());
            savedModule.setCourse(course);
            savedModule.setLessons(Collections.emptyList());

            ModuleDTO expectedDTO = new ModuleDTO(1L, "Test Module", courseId, 0, Collections.emptyList());

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(entityMapper.toEntity(moduleDTO)).thenReturn(module);
            when(moduleRepository.save(any(Module.class))).thenReturn(savedModule);
            when(entityMapper.toDTO(savedModule)).thenReturn(expectedDTO);

            // Act
            ModuleDTO result = moduleService.addModuleToCourse(courseId, moduleDTO);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDTO.id(), result.id());
            assertEquals(expectedDTO.title(), result.title());
            assertEquals(expectedDTO.courseId(), result.courseId());
            verify(courseRepository, times(1)).findById(courseId);
            verify(entityMapper, times(1)).toEntity(moduleDTO);
            verify(moduleRepository, times(1)).save(any(Module.class));
            verify(entityMapper, times(1)).toDTO(savedModule);
        }

        @Test
        void testAddModuleToCourse_CourseNotFound() {
            // Arrange
            Long courseId = 1L;
            ModuleDTO moduleDTO = new ModuleDTO(null, "Test Module", null, 0, Collections.emptyList());

            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> moduleService.addModuleToCourse(courseId, moduleDTO));
            verify(courseRepository, times(1)).findById(courseId);
            verifyNoMoreInteractions(courseRepository, moduleRepository, entityMapper);
        }

        @Test
        void testGetModuleById_Success() {
            // Arrange
            Long moduleId = 1L;
            Module module = new Module();
            module.setId(moduleId);
            module.setTitle("Test Module");
            module.setLessons(Collections.emptyList());

            ModuleDTO expectedDTO = new ModuleDTO(moduleId, "Test Module", 1L, 0, Collections.emptyList());

            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
            when(entityMapper.toDTO(module)).thenReturn(expectedDTO);

            // Act
            ModuleDTO result = moduleService.getModuleById(moduleId);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDTO.id(), result.id());
            assertEquals(expectedDTO.title(), result.title());
            verify(moduleRepository, times(1)).findById(moduleId);
            verify(entityMapper, times(1)).toDTO(module);
        }

        @Test
        void testGetModuleById_NotFound() {
            // Arrange
            Long moduleId = 1L;
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> moduleService.getModuleById(moduleId));
            verify(moduleRepository, times(1)).findById(moduleId);
            verifyNoInteractions(entityMapper);
        }

        @Test
        void testUpdateModule_Success() {
            // Arrange
            Long moduleId = 1L;
            ModuleDTO updatedDTO = new ModuleDTO(moduleId, "Updated Module", 1L, 0, Collections.emptyList());

            Module existingModule = new Module();
            existingModule.setId(moduleId);
            existingModule.setTitle("Original Module");
            existingModule.setLessons(Collections.emptyList());

            Module updatedModule = new Module();
            updatedModule.setId(moduleId);
            updatedModule.setTitle(updatedDTO.title());
            updatedModule.setLessons(Collections.emptyList());

            ModuleDTO expectedDTO = new ModuleDTO(moduleId, "Updated Module", 1L, 0, Collections.emptyList());

            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(existingModule));
            when(moduleRepository.save(any(Module.class))).thenReturn(updatedModule);
            when(entityMapper.toDTO(updatedModule)).thenReturn(expectedDTO);

            // Act
            ModuleDTO result = moduleService.updateModule(moduleId, updatedDTO);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDTO.id(), result.id());
            assertEquals(expectedDTO.title(), result.title());
            verify(moduleRepository, times(1)).findById(moduleId);
            verify(moduleRepository, times(1)).save(any(Module.class));
            verify(entityMapper, times(1)).toDTO(updatedModule);
        }

        @Test
        void testUpdateModule_NotFound() {
            // Arrange
            Long moduleId = 1L;
            ModuleDTO updatedDTO = new ModuleDTO(moduleId, "Updated Module", 1L, 0, Collections.emptyList());

            when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> moduleService.updateModule(moduleId, updatedDTO));
            verify(moduleRepository, times(1)).findById(moduleId);
            verifyNoMoreInteractions(moduleRepository, entityMapper);
        }

        @Test
        void testDeleteModule_Success() {
            // Arrange
            Long moduleId = 1L;
            when(moduleRepository.existsById(moduleId)).thenReturn(true);
            doNothing().when(moduleRepository).deleteById(moduleId);

            // Act
            moduleService.deleteModule(moduleId);

            // Assert
            verify(moduleRepository, times(1)).existsById(moduleId);
            verify(moduleRepository, times(1)).deleteById(moduleId);
        }

        @Test
        void testDeleteModule_NotFound() {
            // Arrange
            Long moduleId = 1L;
            when(moduleRepository.existsById(moduleId)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> moduleService.deleteModule(moduleId));
            verify(moduleRepository, times(1)).existsById(moduleId);
            verifyNoMoreInteractions(moduleRepository);
        }

        @Test
        void testGetModulesByCourseId_Success() {
            // Arrange
            Long courseId = 1L;
            Module module1 = new Module();
            module1.setId(1L);
            module1.setTitle("Module 1");
            module1.setLessons(Collections.emptyList());

            Module module2 = new Module();
            module2.setId(2L);
            module2.setTitle("Module 2");
            module2.setLessons(Collections.emptyList());

            List<Module> modules = List.of(module1, module2);

            ModuleDTO dto1 = new ModuleDTO(1L, "Module 1", courseId, 0, Collections.emptyList());
            ModuleDTO dto2 = new ModuleDTO(2L, "Module 2", courseId, 0, Collections.emptyList());

            when(courseRepository.existsById(courseId)).thenReturn(true);
            when(moduleRepository.findByCourseId(courseId)).thenReturn(modules);
            when(entityMapper.toDTO(module1)).thenReturn(dto1);
            when(entityMapper.toDTO(module2)).thenReturn(dto2);

            // Act
            List<ModuleDTO> results = moduleService.getModulesByCourseId(courseId);

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(dto1.id(), results.get(0).id());
            assertEquals(dto2.id(), results.get(1).id());
            verify(courseRepository, times(1)).existsById(courseId);
            verify(moduleRepository, times(1)).findByCourseId(courseId);
            verify(entityMapper, times(1)).toDTO(module1);
            verify(entityMapper, times(1)).toDTO(module2);
        }

        @Test
        void testGetModulesByCourseId_CourseNotFound() {
            // Arrange
            Long courseId = 1L;
            when(courseRepository.existsById(courseId)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> moduleService.getModulesByCourseId(courseId));
            verify(courseRepository, times(1)).existsById(courseId);
            verifyNoMoreInteractions(courseRepository, moduleRepository, entityMapper);
        }
    }