package com.assylzhana.user_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String email;
    @Column(unique = true)
    private String password;
    private String provider;
    private String providerId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean enabled = false;
    @OneToMany(mappedBy = "user")
    private List<VerificationToken> tokens;
}
