package com.assylzhana.user_service.controller;

import com.assylzhana.user_service.dto.UserRequest;
import com.assylzhana.user_service.model.User;
import com.assylzhana.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }
    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount() {
        try {
            userService.deleteAccount();
            return ResponseEntity.ok("Account deleted successfully.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody UserRequest userRequest) {
        try {
            userService.changePassword(userRequest.getOldPassword(), userRequest.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
}
