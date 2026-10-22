package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourFerieRepository extends JpaRepository<JourFerie, Long> {
    List<JourFerie> findByEtablissementIdOrderByDateJourAsc(Long etablissementId);
}
