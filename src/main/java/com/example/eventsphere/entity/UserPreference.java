package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;

// Table USER_PREFERENCES dans Oracle
// Stocke les préférences choisies à l'étape 2 (image 7 du front)
// TYPE_EVENT : FREE / PAID / BOTH
// VILLE : détectée ou saisie manuellement
@Entity
@Table(name = "USER_PREFERENCES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "TYPE_EVENT", length = 10)
    private String typeEvent = "BOTH";  // FREE / PAID / BOTH

    @Column(name = "VILLE", length = 100)
    private String ville;
}
