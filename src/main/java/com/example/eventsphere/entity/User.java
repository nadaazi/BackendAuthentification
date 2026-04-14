package com.example.eventsphere.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String nom;
    private String mdp;

    @Temporal(TemporalType.DATE)
    private Date dateInscription;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getMdp() { return mdp; }
    public void setMdp(String mdp) { this.mdp = mdp; }
    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date d) { this.dateInscription = d; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}