package com.example.eventsphere.controller;

import com.example.eventsphere.dto.response.ApiResponse;
import com.example.eventsphere.dto.response.EventResponse;
import com.example.eventsphere.dto.response.OrganizerResponse;
import com.example.eventsphere.entity.*;
import com.example.eventsphere.repository.*;
import com.example.eventsphere.security.JwtUtils;
import com.example.eventsphere.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private TicketTypeRepository ticketTypeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private Oauth2CompteRepository oauth2CompteRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private UserPreferenceRepository userPreferenceRepository;

    // ── STATS ─────────────────────────────────────────────────────────
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        long totalOrganizers  = utilisateurRepository.countByRoleNomRole("ORGANISATEUR");
        long totalVisitors    = utilisateurRepository.countByRoleNomRole("VISITEUR");
        long totalEvents      = eventRepository.count();
        long publishedEvents  = eventRepository.countByStatus("active") + eventRepository.countByStatus("upcoming");
        long draftEvents      = eventRepository.countByStatus("draft");
        long cancelledEvents  = eventRepository.countByStatus("cancelled");

        List<TicketType> allTickets = ticketTypeRepository.findAll();
        long   totalTicketsSold = allTickets.stream().mapToLong(t -> t.getSold() == null ? 0 : t.getSold()).sum();
        double totalRevenue     = allTickets.stream()
                .mapToDouble(t -> (t.getPrice() == null ? 0 : t.getPrice()) * (t.getSold() == null ? 0 : t.getSold()))
                .sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOrganizers", totalOrganizers);
        stats.put("totalVisitors", totalVisitors);
        stats.put("totalEvents", totalEvents);
        stats.put("publishedEvents", publishedEvents);
        stats.put("draftEvents", draftEvents);
        stats.put("cancelledEvents", cancelledEvents);
        stats.put("totalTicketsSold", totalTicketsSold);
        stats.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(ApiResponse.ok("OK", stats));
    }

    // ── ORGANISATEURS ─────────────────────────────────────────────────
    @GetMapping("/organizers")
    public ResponseEntity<ApiResponse<List<OrganizerResponse>>> getOrganizers() {
        List<Utilisateur> organizers = utilisateurRepository.findByRoleNomRole("ORGANISATEUR");
        List<OrganizerResponse> result = organizers.stream().map(u -> {
            List<Event> events = eventRepository.findByOrganisateurId(u.getId());
            long ticketsSold = events.stream()
                    .flatMap(e -> e.getTicketTypes() == null ? Stream.empty() : e.getTicketTypes().stream())
                    .mapToLong(t -> t.getSold() == null ? 0 : t.getSold()).sum();
            double revenue = events.stream()
                    .flatMap(e -> e.getTicketTypes() == null ? Stream.empty() : e.getTicketTypes().stream())
                    .mapToDouble(t -> (t.getPrice() == null ? 0 : t.getPrice()) * (t.getSold() == null ? 0 : t.getSold()))
                    .sum();
            return OrganizerResponse.builder()
                    .id(u.getId()).nom(u.getNomComplet()).email(u.getEmail())
                    .eventsCount((long) events.size()).totalTicketsSold(ticketsSold)
                    .totalRevenue(revenue)
                    .verified(u.getEmailVerifie() != null && u.getEmailVerifie() == 1)
                    .build();
        }).toList();
        return ResponseEntity.ok(ApiResponse.ok("OK", result));
    }

    @PostMapping("/organizers")
    public ResponseEntity<ApiResponse<OrganizerResponse>> createOrganizer(@RequestBody Map<String, String> body) {
        String nom = body.get("nom"), email = body.get("email"), password = body.get("password");
        if (nom == null || email == null || password == null || password.length() < 6)
            return ResponseEntity.badRequest().body(ApiResponse.error("Données invalides."));
        if (utilisateurRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(ApiResponse.error("Email déjà utilisé."));

        Role role = roleRepository.findByNomRole("ORGANISATEUR")
                .orElseGet(() -> { Role r = new Role(); r.setNomRole("ORGANISATEUR"); return roleRepository.save(r); });

        Utilisateur saved = utilisateurRepository.save(Utilisateur.builder()
                .nomComplet(nom).email(email)
                .motDePasse(passwordEncoder.encode(password))
                .role(role).estActif(1).emailVerifie(1).build());

        try { emailService.envoyerCredentialsOrganisateur(email, nom, password); } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.ok("Organisateur créé. Identifiants envoyés par email.", OrganizerResponse.builder()
                .id(saved.getId()).nom(saved.getNomComplet()).email(saved.getEmail()).verified(true).build()));
    }

    @PutMapping("/organizers/{id}/verify")
    public ResponseEntity<ApiResponse<OrganizerResponse>> verifyOrganizer(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .map(u -> {
                    u.setEmailVerifie(1); utilisateurRepository.save(u);
                    return ResponseEntity.ok(ApiResponse.ok("Organisateur vérifié.", OrganizerResponse.builder()
                            .id(u.getId()).nom(u.getNomComplet()).email(u.getEmail()).verified(true).build()));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/organizers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganizer(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) return ResponseEntity.notFound().build();
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Organisateur supprimé."));
    }

    // ── TOUS LES UTILISATEURS ─────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() != null && !"ADMIN".equals(u.getRole().getNomRole()))
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("nomComplet", u.getNomComplet());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole().getNomRole());
                    m.put("estActif", u.getEstActif());
                    m.put("emailVerifie", u.getEmailVerifie());
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", users));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) return ResponseEntity.notFound().build();
        emailVerificationTokenRepository.deleteByUtilisateurId(id);
        passwordResetTokenRepository.deleteByUtilisateurId(id);
        oauth2CompteRepository.deleteByUtilisateurId(id);
        userInterestRepository.deleteByUtilisateurId(id);
        userPreferenceRepository.deleteByUtilisateurId(id);
        eventRepository.detachOrganisateur(id);
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Utilisateur supprimé."));
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleUserActive(@PathVariable Long id) {
        return utilisateurRepository.findById(id).map(u -> {
            int newStatus = (u.getEstActif() != null && u.getEstActif() == 1) ? 0 : 1;
            u.setEstActif(newStatus);
            utilisateurRepository.save(u);
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("id", u.getId());
            res.put("estActif", newStatus);
            return ResponseEntity.ok(ApiResponse.ok(newStatus == 1 ? "Compte activé." : "Compte désactivé.", res));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{id}/impersonate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> impersonate(@PathVariable Long id) {
        return utilisateurRepository.findById(id).map(u -> {
            String role = u.getRole() != null ? u.getRole().getNomRole() : "VISITEUR";
            UserPreference pref = userPreferenceRepository.findByUtilisateurId(u.getId()).orElse(null);
            String typeEvent = pref != null ? pref.getTypeEvent() : "BOTH";
            String ville = pref != null ? pref.getVille() : null;
            String token = jwtUtils.generateToken(u.getEmail(), role, u.getId(), typeEvent, ville);
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("token", token);
            res.put("nomComplet", u.getNomComplet());
            res.put("email", u.getEmail());
            res.put("role", role);
            return ResponseEntity.ok(ApiResponse.ok("Impersonification réussie.", res));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── TOUS LES ÉVÉNEMENTS ───────────────────────────────────────────
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllEvents() {
        List<Map<String, Object>> events = eventRepository.findAll().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("date", e.getDate() != null ? e.getDate().toString() : null);
            m.put("location", e.getLocation());
            m.put("status", e.getStatus());
            m.put("category", e.getCategory());
            m.put("organizer", e.getOrganisateur() != null ? e.getOrganisateur().getNomComplet() : "—");
            long sold = e.getTicketTypes() == null ? 0 : e.getTicketTypes().stream()
                    .mapToLong(t -> t.getSold() == null ? 0 : t.getSold()).sum();
            m.put("ticketsSold", sold);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) return ResponseEntity.notFound().build();
        eventRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Événement supprimé."));
    }
}
