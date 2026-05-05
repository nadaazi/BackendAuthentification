package com.example.eventsphere.repository;

import com.example.eventsphere.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByUtilisateurEmailAndOtpCodeAndEstUtilise(
            String email, String otpCode, Integer estUtilise);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.utilisateur.id = :userId")
    void deleteByUtilisateurId(Long userId);
}
