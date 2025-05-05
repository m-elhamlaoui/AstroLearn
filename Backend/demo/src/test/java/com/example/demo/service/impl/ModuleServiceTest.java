package com.example.demo.service.impl;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ModuleMapper;
import com.example.demo.model.Module;
import com.example.demo.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ModuleMapper moduleMapper;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    private Module module;
    private ModuleDTO moduleDTO;
    private Long moduleId = 1L;
    private Long nonExistentModuleId = 999L;

    @BeforeEach
    void setUp() {
        module = new Module();
        module.setId(moduleId);
        module.setTitle("Test Module");
        module.setDescription("Test Description");

        moduleDTO = new ModuleDTO();
        moduleDTO.setId(moduleId);
        moduleDTO.setTitle("Test Module");
        moduleDTO.setDescription("Test Description");
    }

    @Test
    void getModuleById_WhenModuleExists_ShouldReturnModuleDTO() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(moduleMapper.toDto(module)).thenReturn(moduleDTO);

        ModuleDTO result = moduleService.getModuleById(moduleId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(moduleId);
        assertThat(result.getTitle()).isEqualTo(moduleDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(moduleDTO.getDescription());

        verify(moduleRepository).findById(moduleId);
        verify(moduleMapper).toDto(module);
    }

    @Test
    void getModuleById_WhenModuleDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(moduleRepository.findById(nonExistentModuleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.getModuleById(nonExistentModuleId);
        });

        assertThat(exception.getMessage()).contains("Module not found with id: " + nonExistentModuleId);

        verify(moduleRepository).findById(nonExistentModuleId);
        verify(moduleMapper, never()).toDto(any(Module.class));
    }

    @Test
    void getAllModules_ShouldReturnListOfModuleDTOs() {
        List<Module> modules = Arrays.asList(module);
        List<ModuleDTO> moduleDTOs = Arrays.asList(moduleDTO);

        when(moduleRepository.findAll()).thenReturn(modules);
        when(moduleMapper.toDto(module)).thenReturn(moduleDTO);

        List<ModuleDTO> result = moduleService.getAllModules();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(moduleId);
        assertThat(result.get(0).getTitle()).isEqualTo(moduleDTO.getTitle());
        assertThat(result.get(0).getDescription()).isEqualTo(moduleDTO.getDescription());

        verify(moduleRepository).findAll();
        verify(moduleMapper).toDto(module);
    }

    @Test
    void createModule_ShouldReturnCreatedModuleDTO() {
        when(moduleMapper.toEntity(moduleDTO)).thenReturn(module);
        when(moduleRepository.save(module)).thenReturn(module);
        when(moduleMapper.toDto(module)).thenReturn(moduleDTO);

        ModuleDTO result = moduleService.createModule(moduleDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(moduleId);
        assertThat(result.getTitle()).isEqualTo(moduleDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(moduleDTO.getDescription());

        verify(moduleMapper).toEntity(moduleDTO);
        verify(moduleRepository).save(module);
        verify(moduleMapper).toDto(module);
    }

    @Test
    void updateModule_WhenModuleExists_ShouldReturnUpdatedModuleDTO() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(moduleMapper.toEntity(moduleDTO)).thenReturn(module);
        when(moduleRepository.save(module)).thenReturn(module);
        when(moduleMapper.toDto(module)).thenReturn(moduleDTO);

        ModuleDTO result = moduleService.updateModule(moduleId, moduleDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(moduleId);
        assertThat(result.getTitle()).isEqualTo(moduleDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(moduleDTO.getDescription());

        verify(moduleRepository).findById(moduleId);
        verify(moduleMapper).toEntity(moduleDTO);
        verify(moduleRepository).save(module);
        verify(moduleMapper).toDto(module);
    }

    @Test
    void updateModule_WhenModuleDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(moduleRepository.findById(nonExistentModuleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.updateModule(nonExistentModuleId, moduleDTO);
        });

        assertThat(exception.getMessage()).contains("Module not found with id: " + nonExistentModuleId);

        verify(moduleRepository).findById(nonExistentModuleId);
        verify(moduleMapper, never()).toEntity(any(ModuleDTO.class));
        verify(moduleRepository, never()).save(any(Module.class));
        verify(moduleMapper, never()).toDto(any(Module.class));
    }

    @Test
    void deleteModule_WhenModuleExists_ShouldDeleteSuccessfully() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        doNothing().when(moduleRepository).deleteById(moduleId);

        moduleService.deleteModule(moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository).deleteById(moduleId);
    }

    @Test
    void deleteModule_WhenModuleDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(moduleRepository.findById(nonExistentModuleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.deleteModule(nonExistentModuleId);
        });

        assertThat(exception.getMessage()).contains("Module not found with id: " + nonExistentModuleId);

        verify(moduleRepository).findById(nonExistentModuleId);
        verify(moduleRepository, never()).deleteById(anyLong());
    }
}
