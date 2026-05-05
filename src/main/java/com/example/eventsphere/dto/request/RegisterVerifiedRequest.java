package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterVerifiedRequest {

    @NotBlank(message = "Le nom complet est requis.")
    @Size(min = 2, max = 100)
    private String nomComplet;

    @NotBlank(message = "L'email est requis.")
    @Email(message = "Format d'email invalide.")
    private String email;

    @NotBlank(message = "Le téléphone est requis.")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Numéro de téléphone invalide.")
    private String telephone;

    private String documentIdentitePath;
}
