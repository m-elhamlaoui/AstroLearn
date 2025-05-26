package com.example.demo.service.impl;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Course;
import com.example.demo.model.Module;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleServiceImpl implements ModuleService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final EntityMapper entityMapper;

    @Override
    public ModuleDTO addModuleToCourse(Long courseId, ModuleDTO moduleDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        Module module = entityMapper.toEntity(moduleDTO);
        module.setCourse(course);
        module.setLessons(Collections.emptyList());
        Module savedModule = moduleRepository.save(module);
        return entityMapper.toDTO(savedModule);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleDTO getModuleById(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        return entityMapper.toDTO(module);
    }

    @Override
    public ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO) {
        Module existingModule = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        existingModule.setTitle(moduleDTO.title());
        Module updatedModule = moduleRepository.save(existingModule);
        return entityMapper.toDTO(updatedModule);
    }

    @Override
    public void deleteModule(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", "id", moduleId);
        }
        moduleRepository.deleteById(moduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        return moduleRepository.findByCourseId(courseId).stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ModuleDTO> reorderModules(Long courseId, List<Long> moduleIds) {
        // Verify course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        // Fetch all modules for this course
        List<Module> modules = moduleRepository.findByCourseId(courseId);
        
        // Create a map of module ID to module for quick lookup
        java.util.Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, module -> module));
        
        // Update the order of each module based on its position in the moduleIds list
        for (int i = 0; i < moduleIds.size(); i++) {
            Long moduleId = moduleIds.get(i);
            Module module = moduleMap.get(moduleId);
            
            if (module != null) {
                module.setOrderIndex(i);
                moduleRepository.save(module);
            }
        }
        
        // Return the updated modules
        return moduleRepository.findByCourseId(courseId).stream()
                .sorted((m1, m2) -> Integer.compare(m1.getOrderIndex(), m2.getOrderIndex()))
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }
}