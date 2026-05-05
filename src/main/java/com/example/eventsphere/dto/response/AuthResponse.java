package com.example.eventsphere.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private Long id;
    private String nomComplet;
    private String email;
    private String role;
    private Integer emailVerifie;
    private boolean needsSetup;
    private PreferenceInfo preference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceInfo {
        private String typeEvent;
        private String ville;
    }
}
