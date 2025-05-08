package com.example.demo.service.impl;

import com.example.demo.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Override
    public String generatePresignedUrl() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(region))
                .build();

        String key = "uploads/" + UUID.randomUUID() + ".jpg";

         // Do NOT hardcode contentType here. Let the client specify it during upload.
         // S3 will accept the client's Content-Type if it's not part of the signature.
         PutObjectRequest objectRequest = PutObjectRequest.builder()
                 .bucket(bucketName)
                 .key(key)
                 // .contentType("image/jpeg") // REMOVED - Client will send this header
                 .build();
 
         PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        URL url = presigner.presignPutObject(presignRequest).url();

        presigner.close();

        return "{\"uploadUrl\": \"" + url + "\", \"key\": \"" + key + "\"}";
    }
}
