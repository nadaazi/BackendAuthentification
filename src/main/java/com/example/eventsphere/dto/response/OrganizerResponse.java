package com.example.eventsphere.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerResponse {
    private Long id;
    private String nom;
    private String email;
    private Long eventsCount;
    private Long totalTicketsSold;
    private Double totalRevenue;
    private Boolean verified;
}
