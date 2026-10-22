package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.EcheancierPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EcheancierPaiementRepository extends JpaRepository<EcheancierPaiement, Long> {
    List<EcheancierPaiement> findByEleveId(Long eleveId);
}
