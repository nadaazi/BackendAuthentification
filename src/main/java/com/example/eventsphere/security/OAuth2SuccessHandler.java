package com.example.eventsphere.security;

import com.example.eventsphere.entity.*;
import com.example.eventsphere.repository.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;

// Appelé automatiquement par Spring après succès Google OAuth2
// Crée ou retrouve l'utilisateur, génère un JWT, redirige vers le frontend
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired private UtilisateurRepository utilisateurRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private Oauth2CompteRepository oauth2Repo;
    @Autowired private JwtUtils jwtUtils;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication auth) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();

        String email      = oAuth2User.getAttribute("email");
        String nomComplet = oAuth2User.getAttribute("name");
        String googleId   = oAuth2User.getAttribute("sub"); // ID unique Google

        // Cherche si ce compte Google est déjà lié
        Optional<Oauth2Compte> existingOauth = oauth2Repo.findByProviderAndProviderId("google", googleId);

        Utilisateur utilisateur;

        if (existingOauth.isPresent()) {
            // Compte déjà lié → on récupère l'utilisateur
            utilisateur = existingOauth.get().getUtilisateur();
        } else {
            // Première fois avec Google → créer ou lier un compte existant
            Optional<Utilisateur> existingUser = utilisateurRepo.findByEmail(email);

            if (existingUser.isPresent()) {
                utilisateur = existingUser.get();
            } else {
                // Nouveau utilisateur via Google → rôle VISITEUR par défaut
                Role roleVisiteur = roleRepo.findByNomRole("VISITEUR")
                        .orElseThrow(() -> new RuntimeException("Rôle VISITEUR introuvable"));

                utilisateur = utilisateurRepo.save(Utilisateur.builder()
                        .nomComplet(nomComplet)
                        .email(email)
                        .motDePasse("") // pas de mot de passe pour OAuth2
                        .role(roleVisiteur)
                        .emailVerifie(1) // email déjà vérifié par Google
                        .build());
            }

            // Lier le compte Google à cet utilisateur
            oauth2Repo.save(Oauth2Compte.builder()
                    .utilisateur(utilisateur)
                    .provider("google")
                    .providerId(googleId)
                    .build());
        }

        // Générer le JWT
        String token = jwtUtils.generateToken(utilisateur.getEmail(),
                utilisateur.getRole().getNomRole());

        // Rediriger vers le frontend avec le token dans l'URL
        // Le frontend React le récupère et le stocke dans localStorage
        String redirectUrl = frontendUrl + "/auth/oauth2/success?token=" + token
                + "&email=" + email
                + "&nom=" + nomComplet
                + "&role=" + utilisateur.getRole().getNomRole()
                + "&emailVerifie=" + utilisateur.getEmailVerifie();

        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }
}
