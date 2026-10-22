package com.educore.ia.risque.repository;

import com.educore.ia.risque.entity.AlerteRisqueEchec;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlerteRisqueEchecRepository extends JpaRepository<AlerteRisqueEchec, Long> {
    Optional<AlerteRisqueEchec> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<AlerteRisqueEchec> findByPeriodeIdAndScoreRisqueGreaterThanEqualOrderByScoreRisqueDesc(Long periodeId, java.math.BigDecimal seuil);
}
