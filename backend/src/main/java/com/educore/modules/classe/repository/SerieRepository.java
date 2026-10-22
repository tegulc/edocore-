package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    List<Serie> findByEtablissementIdOrderByLibelleAsc(Long etablissementId);
}
