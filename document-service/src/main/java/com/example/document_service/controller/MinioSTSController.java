package com.example.document_service.controller;


import com.example.document_service.service.MinioSTSService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MinioSTSController {

    private final MinioSTSService minioSTSService;

    @GetMapping("/minio/presigned-url")
    public String getPresignedUrl(@RequestParam String bucketName, @RequestParam String objectName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        return minioSTSService.generatePresignedUrl(bucketName, objectName, userId);
    }
}
