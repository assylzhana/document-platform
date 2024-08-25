package com.assylzhana.user_service.controller;

import com.assylzhana.user_service.dto.LoginRequest;
import com.assylzhana.user_service.model.User;
import com.assylzhana.user_service.service.TokenService;
import com.assylzhana.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully. Please check your email for confirmation.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing or invalid");
        }
        String token = authHeader.substring(7);
        tokenService.blacklistToken(token);
        tokenService.removeToken(token);
        request.getSession().invalidate();
        return ResponseEntity.ok("Successfully logged out");
    }


    @GetMapping("/confirm")
    public ResponseEntity<String> confirmUser(@RequestParam("token") String token) {
        try {
            userService.confirmUser(token);
            return ResponseEntity.ok("User confirmed successfully. You can now log in.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid confirmation token");
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            String token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
            tokenService.saveToken(token, loginRequest.getEmail());
            session.setAttribute("userToken", token);
            session.setAttribute("userEmail", loginRequest.getEmail());
            return ResponseEntity.ok("Your token: " + token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    @GetMapping("/session-info")
    public ResponseEntity<String> getSessionInfo(HttpSession session) {
        String token = (String) session.getAttribute("userToken");
        String email = (String) session.getAttribute("userEmail");
        if (token != null && email != null) {
            return ResponseEntity.ok("User token: " + token + ", User email: " + email);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No session data found");
    }

}
