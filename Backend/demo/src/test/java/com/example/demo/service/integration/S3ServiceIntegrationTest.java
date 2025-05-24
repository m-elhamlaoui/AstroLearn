package com.example.demo.service.integration;

import com.example.demo.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.util.TestLogger;
import static com.example.demo.util.TestLogger.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "aws.accessKeyId=testAccessKey",
        "aws.secretAccessKey=testSecretKey",
        "aws.region=us-east-1",
        "aws.bucketName=test-bucket"
})
@ExtendWith(TestLogger.class)

public class S3ServiceIntegrationTest {

    @Autowired
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        logStep("Setting up test data for S3ServiceIntegrationTest");
    }

    @Test
    void testGeneratePresignedUrl() {
        String result = s3Service.generatePresignedUrl();
        assertThat(result).isNotNull();
        assertThat(result).contains("\"uploadUrl\":");
        assertThat(result).contains("\"key\":");
    }
} 
