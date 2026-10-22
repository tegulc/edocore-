package com.educore.modules.personnel.repository;

import com.educore.modules.personnel.entity.DemandeConge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {
    List<DemandeConge> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);
    List<DemandeConge> findByStatutOrderByCreatedAtDesc(String statut);
}
