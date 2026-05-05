package com.example.eventsphere.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class EventServiceClient {

    @Value("${event-service.url}")
    private String eventServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> forward(String path, HttpMethod method,
                                          String body, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(
                eventServiceUrl + path,
                method,
                entity,
                String.class
            );
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("{\"success\":false,\"message\":\"Event service indisponible : " + e.getMessage() + "\"}");
        }
    }
}
