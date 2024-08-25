package com.example.document_service.controller;

import com.example.document_service.dto.UserDto;
import com.example.document_service.model.DocumentMetadata;
import com.example.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class MainController {

    private final DocumentService service;

    @PostMapping
    public ResponseEntity<String> createDocument(@RequestParam("file") MultipartFile multipartFile,
                                                 @RequestParam(value = "title", required = false) String title) {
        try {
            String author = "";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDto userDto) {
                author = userDto.getEmail();
            }

            String fileExtension = "";
            if (multipartFile.getOriginalFilename() != null && multipartFile.getOriginalFilename().contains(".")) {
                String[] parts = multipartFile.getOriginalFilename().split("\\.");
                fileExtension = parts[parts.length - 1];
            }
            String newFilename = title == null ? multipartFile.getOriginalFilename() : title + "." + fileExtension;
            MultipartFile renamedFile = new MockMultipartFile(
                    newFilename,
                    newFilename,
                    multipartFile.getContentType(),
                    multipartFile.getInputStream()
            );
            String filePath = service.uploadFileToMinio(renamedFile);
            DocumentMetadata documentMetadata = DocumentMetadata.builder()
                    .title(title != null ? title : multipartFile.getOriginalFilename().split("\\.")[0])
                    .author(author)
                    .contentPath(filePath)
                    .documentType(fileExtension)
                    .build();
            DocumentMetadata createdDocument = service.createDocument(documentMetadata);
            return new ResponseEntity<>("Document created successfully with ID: " + createdDocument.getId(), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable String id) {
        try {
            DocumentMetadata document = service.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDocument(@PathVariable String id,
                                                 @RequestBody DocumentMetadata documentMetadata) {
        try {
            DocumentMetadata updatedDocument = service.updateDocument(id, documentMetadata);
            return new ResponseEntity<>("Document updated successfully with ID: " + updatedDocument.getId(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable String id) {
        try {
            service.deleteDocument(id);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
