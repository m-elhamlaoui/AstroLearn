package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    // This endpoint is used to check if the backend is running

    @GetMapping("/")
    public String getRoot() {
        return "Welcome to Space Exploration Hub Backend!";
    }
}