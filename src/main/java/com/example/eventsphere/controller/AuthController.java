package com.example.eventsphere.controller;

import com.example.eventsphere.dto.request.*;
import com.example.eventsphere.dto.response.*;
import com.example.eventsphere.service.AuthService;
import com.example.eventsphere.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private EmailService emailService;

    // POST /auth/register — Inscription Visiteur
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
        String fallbackOtp = authService.inscrireVisiteur(req.getNomComplet(), req.getEmail(), req.getMotDePasse());
        if (fallbackOtp != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                "Compte créé ! Email non livré — code OTP : " + fallbackOtp, fallbackOtp));
        }
        return ResponseEntity.ok(ApiResponse.ok("Compte créé ! Vérifiez votre email pour le code OTP.", null));
    }

    // POST /auth/register/verified — Inscription Visiteur Vérifié
    @PostMapping("/register/verified")
    public ResponseEntity<ApiResponse<String>> registerVerified(@Valid @RequestBody RegisterVerifiedRequest req) {
        String fallbackOtp = authService.inscrireVisiteurVerifie(
                req.getNomComplet(), req.getEmail(),
                req.getTelephone(), req.getDocumentIdentitePath());
        if (fallbackOtp != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                "Compte créé ! Email non livré — code OTP : " + fallbackOtp, fallbackOtp));
        }
        return ResponseEntity.ok(ApiResponse.ok("Compte créé ! Vérifiez votre email pour le code OTP.", null));
    }

    // POST /auth/login — Connexion
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse auth = authService.login(req.getEmail(), req.getMotDePasse());
        return ResponseEntity.ok(ApiResponse.ok("Connexion réussie.", auth));
    }

    // POST /auth/verify-otp — Vérification OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpRequest req) {
        AuthResponse auth = authService.verifierOtp(req.getEmail(), req.getOtpCode());
        return ResponseEntity.ok(ApiResponse.ok("Email vérifié avec succès.", auth));
    }

    // POST /auth/resend-otp — Renvoi OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody EmailRequest req) {
        String fallbackOtp = authService.renvoyerOtp(req.getEmail());
        if (fallbackOtp != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                "Email non livré — code OTP : " + fallbackOtp, fallbackOtp));
        }
        return ResponseEntity.ok(ApiResponse.ok("Un nouveau code a été envoyé à votre email.", null));
    }

    // POST /auth/forgot-password — Demande de reset
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody EmailRequest req) {
        try {
            authService.demanderResetPassword(req.getEmail());
        } catch (Exception ignored) {
            // Sécurité : ne pas révéler si l'email existe
        }
        return ResponseEntity.ok(ApiResponse.ok("Si cet email existe, un lien vous a été envoyé."));
    }

    // POST /auth/reset-password — Réinitialisation
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.reinitialiserMotDePasse(req.getToken(), req.getNouveauMotDePasse());
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé avec succès !"));
    }

    // POST /auth/google-token — Google OAuth via frontend
    @PostMapping("/google-token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleToken(@RequestBody Map<String, String> body) {
        try {
            String url;
            @SuppressWarnings("unchecked")
            Map<String, String> info;
            RestTemplate rest = new RestTemplate();

            if (body.containsKey("accessToken")) {
                url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + body.get("accessToken");
                info = rest.getForObject(url, Map.class);
                if (info != null) info.put("sub", info.get("id"));
            } else {
                String idToken = body.get("idToken");
                if (idToken == null || idToken.isBlank())
                    return ResponseEntity.badRequest().body(ApiResponse.error("Token manquant."));
                url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
                info = rest.getForObject(url, Map.class);
            }

            if (info == null || !info.containsKey("email"))
                return ResponseEntity.badRequest().body(ApiResponse.error("Token Google invalide."));

            AuthResponse auth = authService.loginOuCreerViaGoogle(
                info.get("email"), info.get("name"), info.get("sub"));
            return ResponseEntity.ok(ApiResponse.ok("Connexion Google réussie.", auth));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur Google : " + e.getMessage()));
        }
    }

    // POST /auth/setup/interests — Intérêts (JWT requis)
    @PostMapping("/setup/interests")
    public ResponseEntity<ApiResponse<Void>> saveInterests(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody InterestsRequest req) {
        authService.sauvegarderInterets(email, req.getInterets());
        return ResponseEntity.ok(ApiResponse.ok("Intérêts sauvegardés."));
    }

    // POST /auth/setup/preferences — Préférences (JWT requis)
    @PostMapping("/setup/preferences")
    public ResponseEntity<ApiResponse<Void>> savePreferences(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PreferencesRequest req) {
        authService.sauvegarderPreferences(email, req.getTypeEvent(), req.getVille());
        return ResponseEntity.ok(ApiResponse.ok("Profil complété avec succès !"));
    }

    // GET /auth/test-email?to=xxx@gmail.com — Test SMTP (développement uniquement)
    @GetMapping("/test-email")
    public ResponseEntity<ApiResponse<Void>> testEmail(@RequestParam String to) {
        try {
            emailService.envoyerOtpVerification(to, "Test User", "123456");
            return ResponseEntity.ok(ApiResponse.ok("Email de test envoyé à " + to + ". Vérifiez votre boîte et les logs."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Erreur SMTP : " + e.getMessage()));
        }
    }
}
