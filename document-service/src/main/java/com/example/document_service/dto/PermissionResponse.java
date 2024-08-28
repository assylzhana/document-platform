package com.example.document_service.dto;

import lombok.Data;

@Data
public class PermissionResponse {
    private String documentId;
    private String userId;
    private boolean isAllowed;
    private String action;
}