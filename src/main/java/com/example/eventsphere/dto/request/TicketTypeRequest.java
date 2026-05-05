package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeRequest {

    @NotBlank(message = "Le type de ticket est requis.")
    @Size(max = 100)
    private String type;

    @NotNull(message = "Le prix est requis.")
    @Min(value = 0, message = "Le prix ne peut pas être négatif.")
    private Double price;

    @NotNull(message = "La quantité est requise.")
    @Min(value = 1, message = "La quantité doit être au moins 1.")
    private Integer quantity;

    private Boolean enabled = true;
    private List<String> benefits;
}
