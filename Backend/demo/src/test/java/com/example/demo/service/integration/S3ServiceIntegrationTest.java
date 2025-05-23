package com.example.demo.service.integration;

import com.example.demo.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "aws.accessKeyId=testAccessKey",
        "aws.secretAccessKey=testSecretKey",
        "aws.region=us-east-1",
        "aws.bucketName=test-bucket"
})
public class S3ServiceIntegrationTest {

    @Autowired
    private S3Service s3Service;

    @Test
    void testGeneratePresignedUrl() {
        String result = s3Service.generatePresignedUrl();
        assertThat(result).isNotNull();
        assertThat(result).contains("\"uploadUrl\":");
        assertThat(result).contains("\"key\":");
    }
} 