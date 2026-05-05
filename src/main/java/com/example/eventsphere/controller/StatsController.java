package com.example.eventsphere.controller;

import com.example.eventsphere.client.EventServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private EventServiceClient eventServiceClient;

    @GetMapping("/organizer/{organisateurId}")
    public ResponseEntity<String> getOrganizerStats(@PathVariable Long organisateurId,
                                                     HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/organizer/" + organisateurId + "/stats",
            HttpMethod.GET, null,
            request.getHeader("Authorization")
        );
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<String> getEventStats(@PathVariable Long eventId,
                                                 HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/events/" + eventId + "/stats",
            HttpMethod.GET, null,
            request.getHeader("Authorization")
        );
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<String> getParticipants(@PathVariable Long eventId,
                                                   HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/events/" + eventId + "/participants",
            HttpMethod.GET, null,
            request.getHeader("Authorization")
        );
    }
}
