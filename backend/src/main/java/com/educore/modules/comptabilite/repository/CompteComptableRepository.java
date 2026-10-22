package com.educore.modules.comptabilite.repository;

import com.educore.modules.comptabilite.entity.CompteComptable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompteComptableRepository extends JpaRepository<CompteComptable, Long> {
    List<CompteComptable> findByEtablissementId(Long etablissementId);
    Optional<CompteComptable> findByEtablissementIdAndCode(Long etablissementId, String code);
}
