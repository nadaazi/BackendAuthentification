package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @NotNull(message = "Le ticketTypeId est requis.")
    private Long ticketTypeId;

    @Min(value = 1, message = "La quantité doit être au moins 1.")
    private int quantity = 1;
}
