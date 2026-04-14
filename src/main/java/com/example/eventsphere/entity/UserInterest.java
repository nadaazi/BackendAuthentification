package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;

// Table USER_INTERESTS dans Oracle
// Stocke les intérêts choisis à l'étape 1 (images 6 et 8 du front)
// Ex: Music, Technology, Business, Startup, Art, etc.
@Entity
@Table(name = "USER_INTERESTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "INTERET", nullable = false, length = 50)
    private String interet;
}
