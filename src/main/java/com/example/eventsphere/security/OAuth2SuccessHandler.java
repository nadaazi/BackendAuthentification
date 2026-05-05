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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired private UtilisateurRepository utilisateurRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private Oauth2CompteRepository oauth2Repo;
    @Autowired private UserPreferenceRepository userPreferenceRepo;
    @Autowired private JwtUtils jwtUtils;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication auth) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();

        String email      = oAuth2User.getAttribute("email");
        String nomComplet = oAuth2User.getAttribute("name");
        String googleId   = oAuth2User.getAttribute("sub");

        Optional<Oauth2Compte> existingOauth = oauth2Repo.findByProviderAndProviderId("google", googleId);
        Utilisateur utilisateur;

        if (existingOauth.isPresent()) {
            utilisateur = existingOauth.get().getUtilisateur();
        } else {
            Optional<Utilisateur> existingUser = utilisateurRepo.findByEmail(email);
            if (existingUser.isPresent()) {
                utilisateur = existingUser.get();
            } else {
                Role roleVisiteur = roleRepo.findByNomRole("VISITEUR")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setNomRole("VISITEUR");
                        return roleRepo.save(r);
                    });
                utilisateur = utilisateurRepo.save(Utilisateur.builder()
                    .nomComplet(nomComplet)
                    .email(email)
                    .motDePasse("")
                    .role(roleVisiteur)
                    .emailVerifie(1)
                    .estActif(1)
                    .build());
            }
            oauth2Repo.save(Oauth2Compte.builder()
                .utilisateur(utilisateur)
                .provider("google")
                .providerId(googleId)
                .build());
        }

        UserPreference pref = userPreferenceRepo.findByUtilisateurId(utilisateur.getId()).orElse(null);
        String typeEvent = pref != null ? pref.getTypeEvent() : "BOTH";
        String ville = pref != null ? pref.getVille() : null;

        String token = jwtUtils.generateToken(
            utilisateur.getEmail(),
            utilisateur.getRole().getNomRole(),
            utilisateur.getId(),
            typeEvent,
            ville
        );

        String redirectUrl = frontendUrl + "/auth/oauth2/success"
            + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
            + "&email=" + URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8)
            + "&nom=" + URLEncoder.encode(nomComplet != null ? nomComplet : "", StandardCharsets.UTF_8)
            + "&role=" + URLEncoder.encode(utilisateur.getRole().getNomRole(), StandardCharsets.UTF_8)
            + "&emailVerifie=" + utilisateur.getEmailVerifie();

        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }
}
