package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Table PASSWORD_RESET_TOKENS dans Oracle
// Utilisé pour l'écran "Trouble logging in?" (image 4 du front)
@Entity
@Table(name = "PASSWORD_RESET_TOKENS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "TOKEN", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "DATE_EXPIRATION", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "EST_UTILISE")
    private Integer estUtilise = 0;

    @Column(name = "DATE_CREATION")
    private LocalDateTime dateCreation = LocalDateTime.now();
}
