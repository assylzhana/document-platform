package com.assylzhana.user_service.controller;

import com.assylzhana.user_service.dto.UserDto;
import com.assylzhana.user_service.dto.UserRequest;
import com.assylzhana.user_service.model.User;
import com.assylzhana.user_service.service.UserService;
import jakarta.servlet.http.HttpSession;
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
    @DeleteMapping()
    public ResponseEntity<String> deleteAccount(HttpSession session) {
        try {
            userService.deleteAccount();
            session.invalidate();
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
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(userDto);
    }
}
