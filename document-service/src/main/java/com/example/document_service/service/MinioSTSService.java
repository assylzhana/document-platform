package com.example.document_service.service;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class MinioSTSService {

    private final MinioClient minioClient;

    public MinioSTSService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String generatePresignedUrl(String bucketName, String objectName, String userId) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(userId + "/" + objectName)  // User-specific path
                            .expiry(1, TimeUnit.HOURS)  // The URL is valid for 1 hour
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating presigned URL for MinIO", e);
        }
    }
}
