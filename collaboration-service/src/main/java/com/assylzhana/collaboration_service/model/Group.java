package com.assylzhana.collaboration_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_emails", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "email",unique = true)
    private List<String> emails = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_documents", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "document",unique = true)
    private List<String> documents = new ArrayList<>();;
}
