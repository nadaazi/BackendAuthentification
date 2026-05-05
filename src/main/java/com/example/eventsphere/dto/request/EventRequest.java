package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Le titre est requis.")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères.")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères.")
    private String description;

    private String date;
    private String time;

    @Size(max = 200)
    private String location;

    @Size(max = 100)
    private String category;

    private String image;

    @Pattern(regexp = "^(draft|active|upcoming|cancelled)$",
             message = "Status invalide. Valeurs: draft, active, upcoming, cancelled")
    private String status = "draft";
}
