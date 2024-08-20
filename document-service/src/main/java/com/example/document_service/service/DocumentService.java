package com.example.document_service.service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

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

    public void deleteDocument(String id) {
        try {
            DocumentMetadata document = documentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Document not found"));
            deleteFileFromMinio(document.getContentPath());
            documentRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting document with id " + id, e);
        }
    }


    public String uploadFileToMinio(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new RuntimeException("File name is missing");
            }
            String filePath = bucketName + "/" + fileName;

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
            DocumentMetadata document = documentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Document not found"));
            String old = bucketName+"/" + document.getTitle() + "." + document.getDocumentType();
            if (documentMetadata.getTitle() != null) {
                document.setTitle(documentMetadata.getTitle());
                document.setContentPath(bucketName + "/" + document.getTitle() +"."+ document.getDocumentType());
                renameFileInMinio(bucketName, old, (bucketName+"/"+documentMetadata.getTitle()+"."+document.getDocumentType()));
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
}