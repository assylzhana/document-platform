package com.example.document_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private String documentId;
    private String userEmail;
    private String ownerEmail;
    private String action;
}
