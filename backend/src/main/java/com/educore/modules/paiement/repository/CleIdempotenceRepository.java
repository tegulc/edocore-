package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.CleIdempotence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CleIdempotenceRepository extends JpaRepository<CleIdempotence, Long> {
    Optional<CleIdempotence> findByEtablissementIdAndCle(Long etablissementId, String cle);
}
