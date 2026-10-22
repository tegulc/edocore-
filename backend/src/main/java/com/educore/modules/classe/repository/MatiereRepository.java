package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    List<Matiere> findByEtablissementIdOrderByLibelleAsc(Long etablissementId);
    boolean existsByEtablissementIdAndCode(Long etablissementId, String code);
}
