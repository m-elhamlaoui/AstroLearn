package com.example.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Module;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    @EntityGraph(attributePaths = {"lessons"})
    List<Module> findByCourseId(Long courseId);
}
