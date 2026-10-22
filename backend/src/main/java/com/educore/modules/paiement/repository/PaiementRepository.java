package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByEleveIdOrderByDatePaiementDesc(Long eleveId);
    Optional<Paiement> findByReferenceTransaction(String referenceTransaction);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.trancheId = :trancheId AND p.statut = 'confirme'")
    BigDecimal sommePayeeSurTranche(Long trancheId);

    /** Utilise par le Module Dashboard pour l'indicateur "paiements du mois". */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.etablissementId = :etablissementId AND p.statut = 'confirme' " +
        "AND p.datePaiement >= :debutMois AND p.datePaiement < :debutMoisSuivant")
    BigDecimal sommeConfirmeeSurPeriode(Long etablissementId, java.time.Instant debutMois, java.time.Instant debutMoisSuivant);
}
