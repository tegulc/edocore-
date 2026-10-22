package com.educore.modules.comptabilite.repository;

import com.educore.modules.comptabilite.entity.EcritureComptable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EcritureComptableRepository extends JpaRepository<EcritureComptable, Long> {
    List<EcritureComptable> findByEtablissementIdAndDateEcritureBetweenOrderByDateEcritureDesc(Long etablissementId, LocalDate debut, LocalDate fin);
    List<EcritureComptable> findByCompteIdOrderByDateEcritureDesc(Long compteId);
}
