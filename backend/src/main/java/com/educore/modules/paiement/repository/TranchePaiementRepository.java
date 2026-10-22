package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.TranchePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranchePaiementRepository extends JpaRepository<TranchePaiement, Long> {
    List<TranchePaiement> findByEcheancierIdOrderByNumeroTrancheAsc(Long echeancierId);
}
