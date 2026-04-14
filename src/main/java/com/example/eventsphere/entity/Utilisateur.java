package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

// Mappe exactement la table UTILISATEURS d'Oracle
@Entity
@Table(name = "UTILISATEURS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Long id;

    @Column(name = "NOM_COMPLET", nullable = false, length = 100)
    private String nomComplet;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "MOT_DE_PASSE", nullable = false, length = 255)
    private String motDePasse;

    @Column(name = "TELEPHONE", length = 20)
    private String telephone;

    // Chemin du fichier uploadé (Passport, CIN, etc.) - pour VISITEUR_VERIFIE
    @Column(name = "DOCUMENT_IDENTITE", length = 500)
    private String documentIdentite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    private Role role;

    @Column(name = "EST_ACTIF")
    private Integer estActif = 1;

    @Column(name = "EMAIL_VERIFIE")
    private Integer emailVerifie = 0;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_CREATION")
    private Date dateCreation = new Date();
}
