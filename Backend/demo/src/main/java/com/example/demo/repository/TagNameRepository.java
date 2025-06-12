package com.example.demo.repository;

import com.example.demo.model.TagName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagNameRepository extends JpaRepository<TagName, Long> {
    Optional<TagName> findByNameIgnoreCase(String name); // Find TagName by normalized name
}
