package com.example.eventsphere.controller;

import com.example.eventsphere.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ──────────────────────────────────────────────────────────
    // POST /auth/register
    // Image 1 du front : Inscription Visiteur normal
    // Body : { "nomComplet": "...", "email": "...", "motDePasse": "..." }
    // → Crée le compte avec rôle VISITEUR + envoie OTP par email
    // ──────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> inscrireVisiteur(@RequestBody Map<String, String> body) {
        try {
            authService.inscrireVisiteur(
                body.get("nomComplet"),
                body.get("email"),
                body.get("motDePasse")
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte créé ! Vérifiez votre email pour le code OTP."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/register/verified
    // Image 2 du front : Inscription Visiteur Vérifié
    // Body : { "nomComplet": "...", "email": "...", "telephone": "...", "documentIdentitePath": "..." }
    // Note : le fichier est uploadé séparément via /auth/upload-document
    // ──────────────────────────────────────────────────────────
    @PostMapping("/register/verified")
    public ResponseEntity<?> inscrireVisiteurVerifie(@RequestBody Map<String, String> body) {
        try {
            authService.inscrireVisiteurVerifie(
                body.get("nomComplet"),
                body.get("email"),
                body.get("telephone"),
                body.get("documentIdentitePath")
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte créé ! Vérifiez votre email pour le code OTP."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/login
    // Image 3 du front : Connexion par email + mot de passe
    // Body : { "email": "...", "motDePasse": "..." }
    // → Retourne { token, nomComplet, email, role, emailVerifie }
    // ──────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = authService.login(
                body.get("email"),
                body.get("motDePasse")
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/verify-otp
    // Image 5 du front : Vérification du code OTP 6 chiffres
    // Body : { "email": "...", "otpCode": "123456" }
    // → Si valide : retourne JWT + marque email comme vérifié
    // ──────────────────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifierOtp(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = authService.verifierOtp(
                body.get("email"),
                body.get("otpCode")
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/resend-otp
    // Image 5 du front : Bouton "Resend code"
    // Body : { "email": "..." }
    // → Supprime l'ancien OTP et en envoie un nouveau
    // ──────────────────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<?> renvoyerOtp(@RequestBody Map<String, String> body) {
        try {
            authService.renvoyerOtp(body.get("email"));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Un nouveau code a été envoyé à votre email."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/forgot-password
    // Image 4 du front : "Trouble logging in?" → "Send Login Link"
    // Body : { "email": "..." }
    // → Envoie un email avec lien de reset
    // ──────────────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            authService.demanderResetPassword(body.get("email"));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Un lien de réinitialisation a été envoyé à votre email."
            ));
        } catch (Exception e) {
            // On retourne toujours "success" pour ne pas révéler si l'email existe
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Si cet email existe, un lien vous a été envoyé."
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/reset-password
    // Après clic sur le lien reçu par email
    // Body : { "token": "...", "nouveauMotDePasse": "..." }
    // ──────────────────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            authService.reinitialiserMotDePasse(
                body.get("token"),
                body.get("nouveauMotDePasse")
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mot de passe réinitialisé avec succès !"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/setup/interests
    // Images 6 et 8 du front : Étape 1 - sélection des intérêts
    // Header : Authorization: Bearer <JWT>
    // Body : { "interets": ["Music", "Technology", "Photography"] }
    // ──────────────────────────────────────────────────────────
    @PostMapping("/setup/interests")
    public ResponseEntity<?> sauvegarderInterets(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, List<String>> body) {
        try {
            authService.sauvegarderInterets(email, body.get("interets"));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Intérêts sauvegardés."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /auth/setup/preferences
    // Image 7 du front : Étape 2 - type d'événement + localisation
    // Header : Authorization: Bearer <JWT>
    // Body : { "typeEvent": "BOTH", "ville": "Casablanca" }
    // ──────────────────────────────────────────────────────────
    @PostMapping("/setup/preferences")
    public ResponseEntity<?> sauvegarderPreferences(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, String> body) {
        try {
            authService.sauvegarderPreferences(
                email,
                body.get("typeEvent"),
                body.get("ville")
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil complété avec succès !"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
