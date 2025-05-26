package com.example.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<com.example.demo.model.Module, Long> {
    @EntityGraph(attributePaths = {"lessons"})
    List<com.example.demo.model.Module> findByCourseId(Long courseId);
    
    // Find modules by course ID ordered by their sequence
    List<com.example.demo.model.Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}
