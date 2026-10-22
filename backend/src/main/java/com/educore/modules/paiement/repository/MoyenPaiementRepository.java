package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.MoyenPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiement, Long> {
    Optional<MoyenPaiement> findByCode(String code);
}
