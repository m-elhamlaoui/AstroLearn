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
}