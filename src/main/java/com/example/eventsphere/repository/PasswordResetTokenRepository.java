package com.example.eventsphere.repository;

import com.example.eventsphere.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndEstUtilise(String token, Integer estUtilise);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(Long utilisateurId);
}
