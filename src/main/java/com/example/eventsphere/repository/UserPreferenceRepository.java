package com.example.eventsphere.repository;

import com.example.eventsphere.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUtilisateurId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPreference p WHERE p.utilisateur.id = :userId")
    void deleteByUtilisateurId(Long userId);
}
