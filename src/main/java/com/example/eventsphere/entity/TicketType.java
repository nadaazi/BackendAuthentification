package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "TICKET_TYPES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_type", length = 100)
    private String type;

    private Double price;

    private Integer quantity;

    @Builder.Default
    private Integer sold = 0;

    @Builder.Default
    private Boolean enabled = true;

    @Column(length = 1000)
    private String benefits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event event;

    @Transient
    public List<String> getBenefitsList() {
        if (benefits == null || benefits.isBlank()) return Collections.emptyList();
        return Arrays.asList(benefits.split(","));
    }

    public void setBenefitsList(List<String> list) {
        this.benefits = list == null ? "" : String.join(",", list);
    }
}
