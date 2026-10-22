package com.educore.ia.risque.repository;

import com.educore.ia.risque.entity.MoyenneGeneraleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MoyenneGeneraleProjectionRepository extends JpaRepository<MoyenneGeneraleProjection, Long> {
    List<MoyenneGeneraleProjection> findByClasseIdAndPeriodeId(Long classeId, Long periodeId);
    Optional<MoyenneGeneraleProjection> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
}
