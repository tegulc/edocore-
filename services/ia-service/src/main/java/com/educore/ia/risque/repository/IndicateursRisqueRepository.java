package com.educore.ia.risque.repository;

import com.educore.ia.risque.entity.IndicateurRisque;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;

/**
 * Requetes de lecture seule sur des tables geree par educore-backend
 * (presences_eleves, sanctions), necessaires au calcul du score de risque.
 * Utilise une entité fictive (IndicateurRisque) pour satisfaire le contrat générique de JpaRepository.
 * Les méthodes utilisent des native queries et ne retournent que des scalaires (long).
 */
public interface IndicateursRisqueRepository extends Repository<IndicateurRisque, Long> {

    @Query(value = "SELECT COUNT(*) FROM presences_eleves WHERE eleve_id = :eleveId AND type_presence = 'absent' " +
            "AND justifie = false AND date_jour BETWEEN :debut AND :fin", nativeQuery = true)
    long compterAbsencesNonJustifiees(Long eleveId, LocalDate debut, LocalDate fin);

    @Query(value = "SELECT COUNT(*) FROM sanctions WHERE eleve_id = :eleveId", nativeQuery = true)
    long compterSanctions(Long eleveId);
}
