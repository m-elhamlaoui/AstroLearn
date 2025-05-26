package com.example.demo.service;

import com.example.demo.dto.CourseDTO;

import java.util.List;

public interface CourseService {


    CourseDTO createCourse(CourseDTO courseDTO); // Add userId param if creator tracking needed
    CourseDTO getCourseById(Long id);
    List<CourseDTO> getAllCourses();
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    void deleteCourse(Long id); // Deletes a course (admin function). Cascades to modules and lessons.
}