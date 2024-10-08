package com.assylzhana.user_service.service;

import com.assylzhana.user_service.model.Role;
import com.assylzhana.user_service.model.User;
import com.assylzhana.user_service.model.VerificationToken;
import com.assylzhana.user_service.repository.UserRepository;
import com.assylzhana.user_service.repository.VerificationTokenRepository;
import com.assylzhana.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;

    private final VerificationTokenRepository tokenRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(user, token);
        tokenRepository.save(verificationToken);
        String confirmationUrl = "http://localhost:8080/auth/confirm?token=" + token;
        emailService.sendSimpleMessage(user.getEmail(), "Registration Confirmation", confirmationUrl);
        return user;
    }

    public void confirmUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account not verified. Please check your email.");
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void deleteAccount() {
        User user = getCurrentUser();
        userRepository.delete(user);
    }

    public void changePassword( String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(getCurrentUser().getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
