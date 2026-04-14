package com.example.eventsphere.repository;

import com.example.eventsphere.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

// ─── UTILISATEUR ───────────────────────────────
interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
}

// ─── ROLE ──────────────────────────────────────
interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNomRole(String nomRole);
}

// ─── EMAIL VERIFICATION TOKEN ──────────────────
interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByUtilisateurEmailAndOtpCodeAndEstUtilise(
            String email, String otpCode, Integer estUtilise);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.utilisateur.id = :userId")
    void deleteByUtilisateurId(Long userId);
}

// ─── PASSWORD RESET TOKEN ──────────────────────
interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndEstUtilise(String token, Integer estUtilise);
}

// ─── OAUTH2 COMPTE ─────────────────────────────
interface Oauth2CompteRepository extends JpaRepository<Oauth2Compte, Long> {
    Optional<Oauth2Compte> findByProviderAndProviderId(String provider, String providerId);
}

// ─── USER INTERESTS ────────────────────────────
interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUtilisateurId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserInterest i WHERE i.utilisateur.id = :userId")
    void deleteByUtilisateurId(Long userId);
}

// ─── USER PREFERENCES ──────────────────────────
interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUtilisateurId(Long userId);
}
