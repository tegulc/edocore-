package com.educore.modules.infirmerie.repository;

import com.educore.modules.infirmerie.entity.Vaccin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VaccinRepository extends JpaRepository<Vaccin, Long> {
    List<Vaccin> findByEleveIdOrderByDateAdministrationDesc(Long eleveId);
}
