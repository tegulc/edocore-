package com.educore.modules.infirmerie.repository;

import com.educore.modules.infirmerie.entity.UrgenceMedicale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UrgenceMedicaleRepository extends JpaRepository<UrgenceMedicale, Long> {
    List<UrgenceMedicale> findByEleveIdOrderByDateUrgenceDesc(Long eleveId);
}
