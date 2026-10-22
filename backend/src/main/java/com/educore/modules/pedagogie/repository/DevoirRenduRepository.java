package com.educore.modules.pedagogie.repository;

import com.educore.modules.pedagogie.entity.DevoirRendu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DevoirRenduRepository extends JpaRepository<DevoirRendu, Long> {
    List<DevoirRendu> findByDevoirIdOrderByDateSoumissionDesc(Long devoirId);
    Optional<DevoirRendu> findByDevoirIdAndEleveId(Long devoirId, Long eleveId);
    List<DevoirRendu> findByEleveIdOrderByDateSoumissionDesc(Long eleveId);
}
