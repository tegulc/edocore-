package com.educore.notifications.notification.repository;

import com.educore.notifications.notification.entity.UtilisateurContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UtilisateurContactRepository extends JpaRepository<UtilisateurContact, Long> {
    // Contourne volontairement le filtre tenant : ce service n'a pas encore recu le contexte
    // etablissement au moment de router un envoi issu d'un appel service-a-service.
    @Query(value = "SELECT id, email, telephone, langue_preferee FROM utilisateurs WHERE id = :id", nativeQuery = true)
    Optional<UtilisateurContact> trouverParId(Long id);
}
