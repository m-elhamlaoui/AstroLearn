package com.example.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<com.example.demo.model.Module, Long> {
    @EntityGraph(attributePaths = {"lessons"})
    List<com.example.demo.model.Module> findByCourseId(Long courseId);
}
