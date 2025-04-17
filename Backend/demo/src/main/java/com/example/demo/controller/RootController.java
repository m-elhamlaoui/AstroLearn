package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    // This endpoint is used to check if the backend is running

    // and to provide a welcome message.
    // It returns a simple string message when accessed.
    // The endpoint is mapped to the root URL ("/") of the application.

    @GetMapping("/")
    public String getRoot() {
        return "Welcome to Space Exploration Hub Backend!";
    }
}