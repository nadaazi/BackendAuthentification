package com.example.eventsphere.config;

import com.example.eventsphere.entity.Role;
import com.example.eventsphere.entity.Utilisateur;
import com.example.eventsphere.repository.RoleRepository;
import com.example.eventsphere.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByNomRole("ADMIN")
                .orElseGet(() -> { Role r = new Role(); r.setNomRole("ADMIN"); return roleRepository.save(r); });
        roleRepository.findByNomRole("ORGANISATEUR")
                .orElseGet(() -> { Role r = new Role(); r.setNomRole("ORGANISATEUR"); return roleRepository.save(r); });
        roleRepository.findByNomRole("VISITEUR")
                .orElseGet(() -> { Role r = new Role(); r.setNomRole("VISITEUR"); return roleRepository.save(r); });

        if (!utilisateurRepository.existsByEmail("admin@eventsphere.com")) {
            utilisateurRepository.save(Utilisateur.builder()
                    .nomComplet("Administrateur")
                    .email("admin@eventsphere.com")
                    .motDePasse(passwordEncoder.encode("Admin2024!"))
                    .role(adminRole)
                    .estActif(1)
                    .emailVerifie(1)
                    .build());
            System.out.println("=== ADMIN créé : admin@eventsphere.com / Admin2024! ===");
        }roleRepository.findByNomRole("VISITEUR_VERIFIE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setNomRole("VISITEUR_VERIFIE");
                    return roleRepository.save(r);
                });

    }
}
