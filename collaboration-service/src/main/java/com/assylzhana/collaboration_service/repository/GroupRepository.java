package com.assylzhana.collaboration_service.repository;

import com.assylzhana.collaboration_service.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {
    @Query("SELECT g FROM Group g JOIN g.emails e JOIN g.documents d WHERE e = :email AND d = :documentId")
    Optional<Group> findByUserEmailsAndDocumentId(@Param("email") String email, @Param("documentId") String documentId);
}
