package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Table OAUTH2_COMPTES dans Oracle
// Utilisé pour les boutons "Continue with Google" (images 1,2,3 du front)
@Entity
@Table(name = "OAUTH2_COMPTES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oauth2Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "PROVIDER", nullable = false, length = 20)
    private String provider;  // "google"

    @Column(name = "PROVIDER_ID", nullable = false, length = 200)
    private String providerId;  // ID unique Google

    @Column(name = "DATE_CREATION")
    private LocalDateTime dateCreation = LocalDateTime.now();
}
