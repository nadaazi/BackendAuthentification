package com.example.eventsphere.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesRequest {

    @Pattern(regexp = "^(FREE|PAID|BOTH)$", message = "typeEvent doit être FREE, PAID ou BOTH.")
    private String typeEvent = "BOTH";

    private String ville;
}
