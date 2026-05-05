package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "L'email est requis.")
    @Email(message = "Format d'email invalide.")
    private String email;
}
