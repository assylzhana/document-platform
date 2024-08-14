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
public class Doc {
    @Id
    private String id;
    private String title;
    private String author;
    private String contentPath;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    private String version;

}
