package com.example.microlink_content.service.impl;

import com.example.microlink_content.service.FileStorageService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
@Primary
public class S3StorageServiceImpl implements FileStorageService {

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.path}")
    private String s3Path;

    private String bucketName;
    private String filePrefix = "";

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (s3Path != null && s3Path.startsWith("s3://")) {
            String pathWithoutScheme = s3Path.substring(5);
            int slashIndex = pathWithoutScheme.indexOf("/");
            if (slashIndex > 0) {
                this.bucketName = pathWithoutScheme.substring(0, slashIndex);
                this.filePrefix = pathWithoutScheme.substring(slashIndex + 1);
                if (!this.filePrefix.isEmpty() && !this.filePrefix.endsWith("/")) {
                    this.filePrefix += "/";
                }
            } else {
                this.bucketName = pathWithoutScheme;
            }
        }

        if ("YOUR_ACCESS_KEY".equals(accessKey)) {
            return; 
        }
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (s3Client == null) {
            // Fallback for demo if S3 is not configured
            return "https://mock-s3-url.com/" + file.getOriginalFilename();
        }
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String key = filePrefix + fileName;
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}
