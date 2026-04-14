package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;

// Mappe la table ROLES d'Oracle : ADMIN, VISITEUR, VISITEUR_VERIFIE, ORGANISATEUR
@Entity
@Table(name = "ROLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROLE")
    private Long id;

    @Column(name = "NOM_ROLE", nullable = false, unique = true, length = 50)
    private String nomRole;
}
