package com.example.demo.dto;

import lombok.Data;

@Data
public class CourseRequest {
    private String title;
    private String description;
    private Double price;
}