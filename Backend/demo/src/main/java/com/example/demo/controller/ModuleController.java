package com.example.demo.controller;

import com.example.demo.dto.ModuleDTO;
import com.example.demo.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // Add a module to a course
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<ModuleDTO> addModuleToCourse(@PathVariable Long courseId, @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO createdModule = moduleService.addModuleToCourse(courseId, moduleDTO);
        return ResponseEntity.ok(createdModule);
    }

    // Get modules by course ID
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourseId(@PathVariable Long courseId) {
        List<ModuleDTO> modules = moduleService.getModulesByCourseId(courseId);
        return ResponseEntity.ok(modules);
    }

    // Get a module by ID
    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Long moduleId) {
        ModuleDTO module = moduleService.getModuleById(moduleId);
        return ResponseEntity.ok(module);
    }

    // Update a module
    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long moduleId, @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO updatedModule = moduleService.updateModule(moduleId, moduleDTO);
        return ResponseEntity.ok(updatedModule);
    }

    // Delete a module
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }
}