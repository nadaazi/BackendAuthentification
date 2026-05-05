package com.example.eventsphere.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;
    private String category;
    private String image;
    private String status;
    private Long participants;
    private Double revenue;
    private List<TicketTypeResponse> ticketTypes;
}
