package com.example.document_service.controller;

import com.example.document_service.model.DocumentMetadata;
import com.example.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @QueryMapping
    public List<DocumentMetadata> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @QueryMapping
    public DocumentMetadata getDocumentById(@Argument String id) {
        return documentService.getDocumentById(id);
    }

    @MutationMapping
    public DocumentMetadata createDocument(@Argument MultipartFile file,
                              @Argument String title,
                              @Argument String author,
                              @Argument String documentType){
        try {
            return documentService.uploadDocument(file, title, author, documentType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

