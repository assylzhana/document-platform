package com.example.document_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(value = "document")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DocumentMetadata {
    @Id
    private String id;
    private String title;
    private String author;
    private String contentPath;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String documentType;
}
