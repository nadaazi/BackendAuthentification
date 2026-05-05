package com.example.eventsphere.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {
    private Long id;
    private String type;
    private Double price;
    private Integer quantity;
    private Integer sold;
    private Integer available;
    private Boolean enabled;
    private List<String> benefits;
}
