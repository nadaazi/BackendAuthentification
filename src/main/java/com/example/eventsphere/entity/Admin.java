package com.example.eventsphere.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin extends User {

    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}