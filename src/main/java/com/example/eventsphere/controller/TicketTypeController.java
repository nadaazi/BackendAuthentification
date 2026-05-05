package com.example.eventsphere.controller;

import com.example.eventsphere.dto.request.TicketTypeRequest;
import com.example.eventsphere.dto.response.ApiResponse;
import com.example.eventsphere.dto.response.TicketTypeResponse;
import com.example.eventsphere.entity.TicketType;
import com.example.eventsphere.repository.EventRepository;
import com.example.eventsphere.repository.TicketTypeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickettypes")
public class TicketTypeController {

    @Autowired private TicketTypeRepository ticketTypeRepository;
    @Autowired private EventRepository eventRepository;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<TicketTypeResponse>>> getByEvent(@PathVariable Long eventId) {
        List<TicketTypeResponse> tickets = ticketTypeRepository.findByEventId(eventId)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("OK", tickets));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> addTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeRequest req) {

        return eventRepository.findById(eventId)
                .map(event -> {
                    TicketType tt = new TicketType();
                    tt.setType(req.getType());
                    tt.setPrice(req.getPrice());
                    tt.setQuantity(req.getQuantity());
                    tt.setSold(0);
                    tt.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
                    tt.setEvent(event);
                    if (req.getBenefits() != null) tt.setBenefitsList(req.getBenefits());
                    return ResponseEntity.ok(ApiResponse.ok("Type de ticket ajouté.", toDto(ticketTypeRepository.save(tt))));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> updateTicketType(
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeRequest req) {

        return ticketTypeRepository.findById(id)
                .map(tt -> {
                    if (req.getType() != null)     tt.setType(req.getType());
                    if (req.getPrice() != null)    tt.setPrice(req.getPrice());
                    if (req.getQuantity() != null) tt.setQuantity(req.getQuantity());
                    if (req.getEnabled() != null)  tt.setEnabled(req.getEnabled());
                    if (req.getBenefits() != null) tt.setBenefitsList(req.getBenefits());
                    return ResponseEntity.ok(ApiResponse.ok("Ticket mis à jour.", toDto(ticketTypeRepository.save(tt))));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicketType(@PathVariable Long id) {
        if (!ticketTypeRepository.existsById(id)) return ResponseEntity.notFound().build();
        ticketTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Type de ticket supprimé."));
    }

    private TicketTypeResponse toDto(TicketType t) {
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
