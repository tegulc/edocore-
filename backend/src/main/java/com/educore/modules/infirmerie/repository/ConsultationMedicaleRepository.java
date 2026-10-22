package com.educore.modules.infirmerie.repository;

import com.educore.modules.infirmerie.entity.ConsultationMedicale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationMedicaleRepository extends JpaRepository<ConsultationMedicale, Long> {
    List<ConsultationMedicale> findByEleveIdOrderByDateConsultationDesc(Long eleveId);
}
