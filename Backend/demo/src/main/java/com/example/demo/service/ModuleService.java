package com.example.demo.service;

import com.example.demo.dto.ModuleDTO;

import java.util.List;

public interface ModuleService {
    ModuleDTO addModuleToCourse(Long courseId, ModuleDTO moduleDTO);
    ModuleDTO getModuleById(Long moduleId);
    ModuleDTO updateModule(Long moduleId, ModuleDTO moduleDTO);
    void deleteModule(Long moduleId);
    List<ModuleDTO> getModulesByCourseId(Long courseId);
    List<ModuleDTO> reorderModules(Long courseId, List<Long> moduleIds);
}