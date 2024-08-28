package com.assylzhana.collaboration_service.controller;

import com.assylzhana.collaboration_service.dto.EditGroupRequest;
import com.assylzhana.collaboration_service.model.Group;
import com.assylzhana.collaboration_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody Group group) throws ExecutionException, InterruptedException {
        try {
            groupService.createGroup(group);
            return ResponseEntity.ok("Group created successfully");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{groupId}")
    public ResponseEntity<String> editGroup(@PathVariable Long groupId,
                                            @RequestBody EditGroupRequest request) {
        try {
            groupService.editGroup(groupId, request.getEmails(), request.getDocuments());
            return ResponseEntity.ok("Group updated successfully");
        } catch (RuntimeException | ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
