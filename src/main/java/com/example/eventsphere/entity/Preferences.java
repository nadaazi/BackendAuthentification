package com.example.eventsphere.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "preferences")
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String categorie;
    private Double budget;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}