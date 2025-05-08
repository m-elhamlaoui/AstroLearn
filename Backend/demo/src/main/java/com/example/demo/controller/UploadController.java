package com.example.demo.controller;

import com.example.demo.service.S3Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final S3Service s3Service;

    @GetMapping("/generate-upload-url")
    public String generateUrl() {
        return s3Service.generatePresignedUrl();
    }
}
