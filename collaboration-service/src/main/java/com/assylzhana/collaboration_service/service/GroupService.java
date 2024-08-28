package com.assylzhana.collaboration_service.service;

import com.assylzhana.collaboration_service.dto.Emails;
import com.assylzhana.collaboration_service.dto.Permission;
import com.assylzhana.collaboration_service.dto.PermissionResponse;
import com.assylzhana.collaboration_service.model.Group;
import com.assylzhana.collaboration_service.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;


    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, Emails> kafkaTemplate1;
    private final ConcurrentMap<String, CompletableFuture<Boolean>> futures = new ConcurrentHashMap<>();

    @KafkaListener(topics = "permission-check")
    public void checkPermission(Permission event) {
        boolean isAllowed = checkUserPermissions(event.getUserEmail(), event.getOwnerEmail(), event.getDocumentId());
        PermissionResponse responseEvent = new PermissionResponse();
        responseEvent.setDocumentId(event.getDocumentId());
        responseEvent.setUserId(event.getUserEmail());
        responseEvent.setAllowed(isAllowed);
        responseEvent.setAction(event.getAction());

        String permissionResponseTopic = "permission-response";
        kafkaTemplate.send(permissionResponseTopic, String.valueOf(isAllowed));
    }
    private boolean checkUserPermissions(String userEmail, String ownerEmail, String documentId) {
        return userEmail.equals(ownerEmail) || isUserInGroupWithOwner(userEmail, ownerEmail, documentId);
    }

    private boolean isUserInGroupWithOwner(String userEmail, String ownerEmail, String documentId) {
        Optional<Group> group = groupRepository.findByUserEmailsAndDocumentId(ownerEmail, documentId);

        return group.map(value -> value.getEmails().contains(userEmail)).orElse(false);

    }

    public CompletableFuture<Boolean> checkUserExistence(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        futures.put(email, future);
        kafkaTemplate.send("check-user-existence-request", email);
        return future;
    }

    @KafkaListener(topics = "check-user-existence-response", containerFactory = "kafkaListenerContainerFactoryString")
    public void handleUserExistenceResponse(String message) {
        String[] parts = message.split(":");
        String email = parts[0];
        boolean exists = Boolean.parseBoolean(parts[1]);

        CompletableFuture<Boolean> future = futures.remove(email);
        if (future != null) {
            future.complete(exists);
        }
    }

    public CompletableFuture<Boolean> checkDocExistence(String id) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        futures.put(id, future);
        kafkaTemplate.send("check-doc-existence-request", id);
        return future;
    }

    @KafkaListener(topics = "check-doc-existence-response", containerFactory = "kafkaListenerContainerFactoryString")
    public void handleDocExistenceResponse(String message) {
        String[] parts = message.split(":");
        String id = parts[0];
        boolean exists = Boolean.parseBoolean(parts[1]);

        CompletableFuture<Boolean> future = futures.remove(id);
        if (future != null) {
            future.complete(exists);
        }
    }



    @Transactional
    public void createGroup(Group group) throws ExecutionException, InterruptedException {
        List<String> emails = group.getEmails();
        Emails emails1 = Emails.builder()
                .emails(emails)
                .groupName(group.getName()).build();
        List<String> documents = group.getDocuments();
        if (emails == null) {
            throw new RuntimeException("Usernames list is null");
        }
        if (documents == null) {
            throw new RuntimeException("documents list is null");
        }
        log.warn(emails.toString());
        log.warn(documents.toString());

        for (String email : emails) {
            if (!checkUserExistence(email).get()) {
                throw new RuntimeException("User " + email + " does not exist");
            }
        }
        for (String id : documents) {
            if (!checkDocExistence(id).get()) {
                throw new RuntimeException("Doc " + id + " does not exist");
            }
        }
        groupRepository.save(group);
        kafkaTemplate1.send("emails", emails1);
    }

    @Transactional
    public void editGroup(Long groupId, List<String> newEmails, List<String> newDocuments) throws ExecutionException, InterruptedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        List<String> existingEmails = group.getEmails();
        List<String> existingDocuments = group.getDocuments();
        if (newEmails != null) {
            for (String email : newEmails) {
                if (!checkUserExistence(email).get()) {
                    throw new RuntimeException("User " + email + " does not exist");
                }
            }
            existingEmails.addAll(newEmails);
        }
        if (newDocuments != null) {
            for (String id : newDocuments) {
                if (!checkDocExistence(id).get()) {
                    throw new RuntimeException("Doc " + id + " does not exist");
                }
            }
            existingDocuments.addAll(newDocuments);

        }
        groupRepository.save(group);
    }
}
