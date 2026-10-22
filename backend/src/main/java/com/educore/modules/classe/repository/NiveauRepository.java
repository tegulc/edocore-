package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Niveau;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NiveauRepository extends JpaRepository<Niveau, Long> {
    List<Niveau> findByEtablissementIdOrderByOrdreAsc(Long etablissementId);
    boolean existsByEtablissementIdAndCode(Long etablissementId, String code);
    Optional<Niveau> findByEtablissementIdAndCode(Long etablissementId, String code);
}
