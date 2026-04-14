package com.example.eventsphere.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client extends User {

    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}