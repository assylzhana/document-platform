package com.example.document_service.controller;

import com.example.document_service.model.Doc;
import com.example.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @QueryMapping
    public List<Doc> getAllDocuments(){
        return documentService.getAllDocuments();
    }
    @QueryMapping
    public Doc getDocumentById(@Argument String id){
        return documentService.getDocumentById(id);
    }
}
