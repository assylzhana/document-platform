package com.example.document_service.service;

import com.example.document_service.dto.Permission;
import com.example.document_service.dto.UserDto;
import com.example.document_service.exception.MinioUploadException;
import com.example.document_service.model.DocumentMetadata;
import com.example.document_service.repository.DocumentRepository;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final KafkaTemplate<String, Permission> kafkaTemplate;
    private CountDownLatch latch = new CountDownLatch(1);
    private AtomicReference<String> permissionResponse = new AtomicReference<>();

    public List<DocumentMetadata> getAllDocuments() {
        return documentRepository.findAll();
    }

    public DocumentMetadata getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }


    public DocumentMetadata createDocument(DocumentMetadata documentMetadata) {
        documentMetadata.setCreatedAt(OffsetDateTime.now());
        documentMetadata.setUpdatedAt(OffsetDateTime.now());
        return documentRepository.save(documentMetadata);
    }




    public String uploadFileToMinio(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new RuntimeException("File name is missing");
            }
            String userId = getCurrentUserId();
            String filePath = bucketName + "/" + userId + "/" + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return filePath;
        } catch (IOException e) {
            throw new MinioUploadException("Error uploading file to MinIO", e);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    //graphql part
    public DocumentMetadata uploadDocument(MultipartFile file, String title, String author, String documentType) {
        String fileName = file.getOriginalFilename();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new MinioUploadException("Failed to upload document to MinIO", e);
        }
        String fileUrl = "http://localhost:9000/" + bucketName + "/" + fileName;
        DocumentMetadata document = DocumentMetadata.builder()
                .title(title)
                .author(author)
                .contentPath(fileUrl)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .documentType(documentType)
                .build();

        return documentRepository.save(document);
    }

    private void deleteFileFromMinio(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from MinIO", e);
        }
    }

    public DocumentMetadata updateDocument(String id, DocumentMetadata documentMetadata) {
        try {
            String userId = getCurrentUserId();
            DocumentMetadata document = documentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Document not found"));

            Permission event = new Permission();
            event.setDocumentId(id);
            event.setUserEmail(userId);
            event.setOwnerEmail(document.getAuthor());
            event.setAction("UPDATE");

            kafkaTemplate.send("permission-check", event);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error waiting for permission response", e);
            }

            String response = permissionResponse.get();
            if (response.equals("false")) {
                throw new RuntimeException("You do not have permission to edit this document.");
            }
            latch = new CountDownLatch(1);
            permissionResponse.set(null);

            String old = bucketName+"/" + document.getAuthor()+ "/" + document.getTitle() + "." + document.getDocumentType();
            if (documentMetadata.getTitle() != null) {
                document.setTitle(documentMetadata.getTitle());
                document.setContentPath(bucketName + "/"  +document.getAuthor()+ "/"+ document.getTitle() +"."+ document.getDocumentType());
                renameFileInMinio(bucketName, old, (bucketName+"/"+ document.getAuthor()+ "/"+documentMetadata.getTitle()+"."+document.getDocumentType()));
            }
            if (documentMetadata.getAuthor() != null) {
                document.setAuthor(documentMetadata.getAuthor());
            }
            document.setUpdatedAt(OffsetDateTime.now());
            return documentRepository.save(document);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating document with id " + id, e);
        }
    }

    public void deleteDocument(String id) {
        try {
            String userId = getCurrentUserId();
            DocumentMetadata document = documentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Document not found"));

            Permission event = new Permission();
            event.setDocumentId(id);
            event.setUserEmail(userId);
            event.setOwnerEmail(document.getAuthor());
            event.setAction("DELETE");

            kafkaTemplate.send("permission-check", event);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error waiting for permission response", e);
            }

            String response = permissionResponse.get();
            if (response.equals("false")) {
                throw new RuntimeException("You do not have permission to edit this document.");
            }
            latch = new CountDownLatch(1);
            permissionResponse.set(null);

            deleteFileFromMinio(document.getContentPath());
            documentRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting document with id " + id, e);
        }
    }

    @KafkaListener(topics = "permission-response")
    public void onPermissionResponse(String event) {
        log.warn(event);
        permissionResponse.set(event);
        latch.countDown();
    }


    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDto userDto) {
            return userDto.getEmail();
        }
        throw new RuntimeException("User is not authenticated");
    }

    @SneakyThrows
    public void renameFileInMinio(String bucketName, String oldTitle, String newTitle) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(oldTitle)
                    .build());
            log.info("Old file '{}' exists in MinIO.", oldTitle);
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newTitle)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(oldTitle)
                                    .build())
                            .build());

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(oldTitle)
                            .build());

        } catch (ErrorResponseException e) {
            log.error("MinIO Error: {}", e.errorResponse().message(), e);
            throw new RuntimeException("MinIO Error: " + e.errorResponse().message(), e);
        }
    }

    public boolean existsById(String id) {
        return documentRepository.findById(id).isPresent();
    }

    private final KafkaTemplate<String, String> kafkaTemplate1;

    @KafkaListener(topics = "check-doc-existence-request")
    public void checkDocExistence(String id) {
        boolean exists = existsById(id);
        kafkaTemplate1.send("check-doc-existence-response", id + ":" + exists);
    }
}
