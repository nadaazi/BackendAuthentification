package com.example.eventsphere.repository;

import com.example.eventsphere.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByRoleNomRole(String nomRole);
    long countByRoleNomRole(String nomRole);
}
