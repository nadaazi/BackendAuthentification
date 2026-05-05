package com.example.eventsphere.controller;

import com.example.eventsphere.dto.request.EventRequest;
import com.example.eventsphere.dto.request.PurchaseRequest;
import com.example.eventsphere.dto.response.ApiResponse;
import com.example.eventsphere.dto.response.EventResponse;
import com.example.eventsphere.dto.response.TicketTypeResponse;
import com.example.eventsphere.entity.*;
import com.example.eventsphere.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired private EventRepository eventRepository;
    @Autowired private TicketTypeRepository ticketTypeRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> events = eventRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EventResponse>>> searchEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String typeEvent) {
        List<EventResponse> events = eventRepository.findAll().stream()
                .filter(e -> {
                    boolean matchQ = q == null || q.isBlank()
                            || (e.getTitle() != null && e.getTitle().toLowerCase().contains(q.toLowerCase()))
                            || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q.toLowerCase()))
                            || (e.getLocation() != null && e.getLocation().toLowerCase().contains(q.toLowerCase()));
                    boolean matchCat = categorie == null || categorie.isBlank()
                            || (e.getCategory() != null && e.getCategory().equalsIgnoreCase(categorie));
                    boolean matchType = typeEvent == null || typeEvent.isBlank() || typeEvent.equalsIgnoreCase("BOTH")
                            || (typeEvent.equalsIgnoreCase("FREE") && e.getTicketTypes() != null
                                && e.getTicketTypes().stream().allMatch(t -> t.getPrice() == null || t.getPrice() == 0))
                            || (typeEvent.equalsIgnoreCase("PAID") && e.getTicketTypes() != null
                                && e.getTicketTypes().stream().anyMatch(t -> t.getPrice() != null && t.getPrice() > 0));
                    return matchQ && matchCat && matchType;
                })
                .map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = eventRepository.findAll().stream()
                .map(e -> e.getCategory())
                .filter(c -> c != null && !c.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", categories));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcoming() {
        List<EventResponse> events = eventRepository.findAll().stream()
                .filter(e -> "upcoming".equals(e.getStatus()) || "active".equals(e.getStatus()))
                .map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @GetMapping("/free")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getFree() {
        List<EventResponse> events = eventRepository.findAll().stream()
                .filter(e -> e.getTicketTypes() == null || e.getTicketTypes().isEmpty()
                        || e.getTicketTypes().stream().allMatch(t -> t.getPrice() == null || t.getPrice() == 0))
                .map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getByOrganizer(@PathVariable Long organizerId) {
        List<EventResponse> events = eventRepository.findByOrganisateurId(organizerId)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.ok("OK", toDto(e))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest req,
            @AuthenticationPrincipal String email) {

        Utilisateur organisateur = utilisateurRepository.findByEmail(email).orElse(null);

        Event event = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .date(req.getDate() != null ? LocalDate.parse(req.getDate()) : null)
                .time(req.getTime())
                .location(req.getLocation())
                .category(req.getCategory())
                .image(req.getImage())
                .status(req.getStatus() != null ? req.getStatus() : "draft")
                .organisateur(organisateur)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Événement créé.", toDto(eventRepository.save(event))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest req) {

        return eventRepository.findById(id)
                .map(event -> {
                    if (req.getTitle() != null)       event.setTitle(req.getTitle());
                    if (req.getDescription() != null) event.setDescription(req.getDescription());
                    if (req.getDate() != null)        event.setDate(LocalDate.parse(req.getDate()));
                    if (req.getTime() != null)        event.setTime(req.getTime());
                    if (req.getLocation() != null)    event.setLocation(req.getLocation());
                    if (req.getCategory() != null)    event.setCategory(req.getCategory());
                    if (req.getImage() != null)       event.setImage(req.getImage());
                    if (req.getStatus() != null)      event.setStatus(req.getStatus());
                    return ResponseEntity.ok(ApiResponse.ok("Événement mis à jour.", toDto(eventRepository.save(event))));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) return ResponseEntity.notFound().build();
        eventRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Événement supprimé."));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<ApiResponse<Void>> purchaseTicket(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRequest req) {

        return ticketTypeRepository.findById(req.getTicketTypeId())
                .map(tt -> {
                    int available = tt.getQuantity() - (tt.getSold() == null ? 0 : tt.getSold());
                    if (available < req.getQuantity()) {
                        return ResponseEntity.badRequest()
                                .<ApiResponse<Void>>body(ApiResponse.error("Stock insuffisant. Disponible : " + available));
                    }
                    tt.setSold((tt.getSold() == null ? 0 : tt.getSold()) + req.getQuantity());
                    ticketTypeRepository.save(tt);
                    return ResponseEntity.ok(ApiResponse.<Void>ok(
                            req.getQuantity() + " ticket(s) achetés avec succès."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── DTO MAPPERS ────────────────────────────────────────────────
    private EventResponse toDto(Event e) {
        List<TicketType> tickets = e.getTicketTypes() != null ? e.getTicketTypes() : Collections.emptyList();
        long participants = tickets.stream().mapToLong(t -> t.getSold() == null ? 0 : t.getSold()).sum();
        double revenue = tickets.stream()
                .mapToDouble(t -> (t.getPrice() == null ? 0 : t.getPrice()) * (t.getSold() == null ? 0 : t.getSold()))
                .sum();

        return EventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .date(e.getDate() != null ? e.getDate().toString() : null)
                .time(e.getTime())
                .location(e.getLocation())
                .category(e.getCategory())
                .image(e.getImage())
                .status(e.getStatus())
                .participants(participants)
                .revenue(revenue)
                .ticketTypes(tickets.stream().map(this::toTicketDto).collect(Collectors.toList()))
                .build();
    }

    private TicketTypeResponse toTicketDto(TicketType t) {
        int sold = t.getSold() == null ? 0 : t.getSold();
        int qty  = t.getQuantity() == null ? 0 : t.getQuantity();
        return TicketTypeResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .price(t.getPrice())
                .quantity(qty)
                .sold(sold)
                .available(qty - sold)
                .enabled(t.getEnabled())
                .benefits(t.getBenefitsList())
                .build();
    }
}
