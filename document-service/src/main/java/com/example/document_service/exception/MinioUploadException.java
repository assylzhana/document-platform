package com.example.document_service.exception;

public class MinioUploadException extends RuntimeException {
    public MinioUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}