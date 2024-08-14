package com.example.document_service.repository;

import com.example.document_service.model.Doc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<Doc, String> {
}
