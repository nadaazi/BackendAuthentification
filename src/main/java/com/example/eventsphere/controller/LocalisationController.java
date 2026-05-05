package com.example.eventsphere.controller;

import com.example.eventsphere.client.EventServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/localisations")
public class LocalisationController {

    @Autowired
    private EventServiceClient eventServiceClient;

    @GetMapping
    public ResponseEntity<String> getAll() {
        return eventServiceClient.forward("/api/localisations", HttpMethod.GET, null, null);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String body,
                                          HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/localisations",
            HttpMethod.POST, body,
            request.getHeader("Authorization")
        );
    }
}
