package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Table EMAIL_VERIFICATION_TOKENS dans Oracle
// Utilisé pour l'écran "Check your inbox" (image 5 du front)
@Entity
@Table(name = "EMAIL_VERIFICATION_TOKENS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "OTP_CODE", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "DATE_EXPIRATION", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "EST_UTILISE")
    private Integer estUtilise = 0;

    @Column(name = "DATE_CREATION")
    private LocalDateTime dateCreation = LocalDateTime.now();
}
