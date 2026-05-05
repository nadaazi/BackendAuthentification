package com.example.eventsphere.service;

import com.example.eventsphere.dto.response.AuthResponse;
import com.example.eventsphere.entity.*;
import com.example.eventsphere.repository.*;
import com.example.eventsphere.security.JwtUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AuthService {

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private Oauth2CompteRepository oauth2CompteRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EmailVerificationTokenRepository emailTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private UserPreferenceRepository userPreferenceRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private EmailService emailService;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    // ─── INSCRIPTION VISITEUR ───────────────────────────────────────
    // Retourne le code OTP si l'email n'a pas pu être livré (fallback visible au client)
    public String inscrireVisiteur(String nomComplet, String email, String motDePasse) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }
        Utilisateur u = Utilisateur.builder()
                .nomComplet(nomComplet)
                .email(email)
                .motDePasse(passwordEncoder.encode(motDePasse))
                .role(getOrCreateRole("VISITEUR"))
                .estActif(1)
                .emailVerifie(0)
                .build();
        utilisateurRepository.save(u);
        return envoyerOtp(u);
    }

    // ─── INSCRIPTION VISITEUR VERIFIE ───────────────────────────────
    public String inscrireVisiteurVerifie(String nomComplet, String email,
                                        String telephone, String documentIdentitePath) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }
        Utilisateur u = Utilisateur.builder()
                .nomComplet(nomComplet)
                .email(email)
                .motDePasse(passwordEncoder.encode(UUID.randomUUID().toString()))
                .telephone(telephone)
                .documentIdentite(documentIdentitePath)
                .role(getOrCreateRole("VISITEUR_VERIFIE"))
                .estActif(1)
                .emailVerifie(0)
                .build();
        utilisateurRepository.save(u);
        return envoyerOtp(u);
    }

    // ─── LOGIN ──────────────────────────────────────────────────────
    public AuthResponse login(String email, String motDePasse) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect."));

        if (!passwordEncoder.matches(motDePasse, u.getMotDePasse())) {
            throw new RuntimeException("Email ou mot de passe incorrect.");
        }
        if (u.getEstActif() == null || u.getEstActif() == 0) {
            throw new RuntimeException("Compte désactivé. Contactez le support.");
        }
        if (u.getEmailVerifie() == null || u.getEmailVerifie() == 0) {
            throw new RuntimeException("Email non vérifié. Vérifiez votre boîte mail pour le code OTP.");
        }

        return buildAuthResponse(u);
    }

    // ─── VERIFIER OTP ───────────────────────────────────────────────
    public AuthResponse verifierOtp(String email, String otpCode) {
        EmailVerificationToken token = emailTokenRepository
                .findByUtilisateurEmailAndOtpCodeAndEstUtilise(email, otpCode, 0)
                .orElseThrow(() -> new RuntimeException("Code OTP invalide ou expiré."));

        if (token.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code OTP expiré. Demandez un nouveau code.");
        }

        token.setEstUtilise(1);
        emailTokenRepository.save(token);

        Utilisateur u = token.getUtilisateur();
        u.setEmailVerifie(1);
        utilisateurRepository.save(u);

        return buildAuthResponse(u);
    }

    // ─── RENVOYER OTP ───────────────────────────────────────────────
    public String renvoyerOtp(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte avec cet email."));
        emailTokenRepository.deleteByUtilisateurId(u.getId());
        return envoyerOtp(u);
    }

    // ─── RESET PASSWORD REQUEST ─────────────────────────────────────
    public void demanderResetPassword(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte avec cet email."));

        // Invalider les anciens tokens
        passwordResetTokenRepository.deleteByUtilisateurId(u.getId());

        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .utilisateur(u)
                .token(token)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .estUtilise(0)
                .build());

        emailService.envoyerLienResetPassword(u.getEmail(), u.getNomComplet(), token);
    }

    // ─── RESET PASSWORD ─────────────────────────────────────────────
    public void reinitialiserMotDePasse(String token, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndEstUtilise(token, 0)
                .orElseThrow(() -> new RuntimeException("Token invalide ou déjà utilisé."));

        if (resetToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré. Veuillez refaire une demande.");
        }

        Utilisateur u = resetToken.getUtilisateur();
        u.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(u);

        resetToken.setEstUtilise(1);
        passwordResetTokenRepository.save(resetToken);
    }

    // ─── SAUVEGARDER INTERETS ───────────────────────────────────────
    public void sauvegarderInterets(String email, List<String> interets) {
        Utilisateur u = getUtilisateurByEmail(email);
        userInterestRepository.deleteByUtilisateurId(u.getId());
        if (interets != null) {
            interets.forEach(interet ->
                userInterestRepository.save(UserInterest.builder()
                        .utilisateur(u).interet(interet).build()));
        }
    }

    // ─── SAUVEGARDER PREFERENCES ────────────────────────────────────
    public void sauvegarderPreferences(String email, String typeEvent, String ville) {
        Utilisateur u = getUtilisateurByEmail(email);
        UserPreference pref = userPreferenceRepository.findByUtilisateurId(u.getId())
                .orElse(UserPreference.builder().utilisateur(u).build());
        pref.setTypeEvent(typeEvent != null ? typeEvent : "BOTH");
        pref.setVille(ville);
        userPreferenceRepository.save(pref);
    }

    // ─── LOGIN OU CREER VIA GOOGLE ──────────────────────────────────
    public AuthResponse loginOuCreerViaGoogle(String email, String nomComplet, String googleId) {
        Optional<Oauth2Compte> existingOauth = oauth2CompteRepository.findByProviderAndProviderId("google", googleId);
        Utilisateur utilisateur;
        boolean isNewUser = false;
        if (existingOauth.isPresent()) {
            utilisateur = existingOauth.get().getUtilisateur();
        } else {
            Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                utilisateur = existingUser.get();
            } else {
                utilisateur = utilisateurRepository.save(Utilisateur.builder()
                    .nomComplet(nomComplet != null ? nomComplet : email)
                    .email(email)
                    .motDePasse(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(getOrCreateRole("VISITEUR"))
                    .emailVerifie(1)
                    .estActif(1)
                    .build());
                isNewUser = true;
            }
            oauth2CompteRepository.save(Oauth2Compte.builder()
                .utilisateur(utilisateur)
                .provider("google")
                .providerId(googleId)
                .build());
        }
        if (isNewUser) {
            try {
                emailService.envoyerBienvenueGoogle(utilisateur.getEmail(), utilisateur.getNomComplet());
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(AuthService.class)
                    .warn("Email bienvenue Google non envoyé à {} : {}", utilisateur.getEmail(), e.getMessage());
            }
        }
        AuthResponse res = buildAuthResponse(utilisateur);
        res.setNeedsSetup(isNewUser);
        return res;
    }

    // ─── HELPERS ────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(Utilisateur u) {
        UserPreference pref = userPreferenceRepository.findByUtilisateurId(u.getId()).orElse(null);
        String typeEvent = pref != null ? pref.getTypeEvent() : "BOTH";
        String ville = pref != null ? pref.getVille() : null;

        return AuthResponse.builder()
                .token(jwtUtils.generateToken(u.getEmail(), u.getRole().getNomRole(), u.getId(), typeEvent, ville))
                .id(u.getId())
                .nomComplet(u.getNomComplet())
                .email(u.getEmail())
                .role(u.getRole().getNomRole())
                .emailVerifie(u.getEmailVerifie())
                .preference(AuthResponse.PreferenceInfo.builder()
                        .typeEvent(typeEvent)
                        .ville(ville)
                        .build())
                .build();
    }

    // Retourne null si email envoyé, sinon retourne le code OTP (fallback)
    private String envoyerOtp(Utilisateur u) {
        String code = String.format("%06d", new Random().nextInt(999999));
        emailTokenRepository.save(EmailVerificationToken.builder()
                .utilisateur(u)
                .otpCode(code)
                .dateExpiration(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .estUtilise(0)
                .build());
        try {
            emailService.envoyerOtpVerification(u.getEmail(), u.getNomComplet(), code);
            return null; // email livré avec succès
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AuthService.class)
                .error("SMTP ERREUR — OTP non envoyé à {} : {}", u.getEmail(), e.getMessage(), e);
            return code; // email échoué — retourner le code pour fallback
        }
    }

    private Role getOrCreateRole(String nomRole) {
        return roleRepository.findByNomRole(nomRole)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setNomRole(nomRole);
                    return roleRepository.save(r);
                });
    }

    private Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }
}
