package com.example.eventsphere.controller;

import com.example.eventsphere.dto.response.ApiResponse;
import com.example.eventsphere.entity.Utilisateur;
import com.example.eventsphere.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UtilisateurRepository utilisateurRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMe(@AuthenticationPrincipal String email) {
        return utilisateurRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("OK", toMap(u))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMe(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, String> body) {
        return utilisateurRepository.findByEmail(email)
                .map(u -> {
                    if (body.containsKey("nomComplet")) u.setNomComplet(body.get("nomComplet"));
                    if (body.containsKey("telephone"))  u.setTelephone(body.get("telephone"));
                    utilisateurRepository.save(u);
                    return ResponseEntity.ok(ApiResponse.ok("Profil mis à jour.", toMap(u)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(Utilisateur u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nomComplet", u.getNomComplet());
        m.put("email", u.getEmail());
        m.put("telephone", u.getTelephone());
        m.put("role", u.getRole() != null ? u.getRole().getNomRole() : null);
        m.put("emailVerifie", u.getEmailVerifie());
        return m;
    }
}
