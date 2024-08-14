package com.example.document_service.service;

import com.example.document_service.model.Doc;
import com.example.document_service.repository.DocumentRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public List<Doc> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Doc getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }
}
