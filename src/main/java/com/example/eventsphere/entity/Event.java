package com.example.eventsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "EVENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "event_date")
    private LocalDate date;

    @Column(name = "event_time", length = 10)
    private String time;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String image;

    @Column(length = 20)
    @Builder.Default
    private String status = "draft";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id")
    private Utilisateur organisateur;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TicketType> ticketTypes;
}
