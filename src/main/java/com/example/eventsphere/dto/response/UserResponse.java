package com.example.eventsphere.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nomComplet;
    private String email;
    private String telephone;
    private String role;
    private Boolean emailVerifie;
    private Boolean actif;
}
