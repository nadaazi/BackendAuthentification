package com.example.eventsphere.repository;

import com.example.eventsphere.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganisateurId(Long organisateurId);
    long countByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.organisateur = null WHERE e.organisateur.id = :userId")
    void detachOrganisateur(Long userId);
}
