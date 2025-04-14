package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    /**
     * This method handles GET requests to the root URL ("/").
     * It returns a welcome message.
     *
     * @return A welcome message as a String.
     */
    @GetMapping("/")
    public String getRoot() {
        return "Welcome to Space Exploration Hub Backend!";
    }
}