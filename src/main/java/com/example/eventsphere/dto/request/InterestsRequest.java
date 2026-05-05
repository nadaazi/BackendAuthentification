package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestsRequest {

    @NotNull(message = "La liste des intérêts est requise.")
    @Size(min = 3, message = "Veuillez sélectionner au moins 3 intérêts.")
    private List<String> interets;
}
