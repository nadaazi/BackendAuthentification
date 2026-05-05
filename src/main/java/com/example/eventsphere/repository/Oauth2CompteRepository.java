package com.example.eventsphere.repository;

import com.example.eventsphere.entity.Oauth2Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface Oauth2CompteRepository extends JpaRepository<Oauth2Compte, Long> {
    Optional<Oauth2Compte> findByProviderAndProviderId(String provider, String providerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Oauth2Compte o WHERE o.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(Long utilisateurId);
}
