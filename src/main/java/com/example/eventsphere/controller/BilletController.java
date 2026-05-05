package com.example.eventsphere.controller;

import com.example.eventsphere.client.EventServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/billets")
public class BilletController {

    @Autowired
    private EventServiceClient eventServiceClient;

    @GetMapping("/user/{userId}")
    public ResponseEntity<String> getBilletsByUser(@PathVariable Long userId,
                                                    HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/billets/user/" + userId,
            HttpMethod.GET, null,
            request.getHeader("Authorization")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getBillet(@PathVariable Long id,
                                             HttpServletRequest request) {
        return eventServiceClient.forward(
            "/api/billets/" + id,
            HttpMethod.GET, null,
            request.getHeader("Authorization")
        );
    }
}
