package com.educore.modules.emploidutemps.repository;

import com.educore.modules.emploidutemps.entity.CreneauHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, Long> {
    List<CreneauHoraire> findByEtablissementIdOrderByOrdreAsc(Long etablissementId);
}
