package com.example.eventsphere.repository;

import com.example.eventsphere.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUtilisateurId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserInterest i WHERE i.utilisateur.id = :userId")
    void deleteByUtilisateurId(Long userId);
}
