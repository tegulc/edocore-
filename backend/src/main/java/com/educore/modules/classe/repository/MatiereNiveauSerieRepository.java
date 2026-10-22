package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.MatiereNiveauSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatiereNiveauSerieRepository extends JpaRepository<MatiereNiveauSerie, Long> {
    List<MatiereNiveauSerie> findByEtablissementIdAndNiveauId(Long etablissementId, Long niveauId);
    List<MatiereNiveauSerie> findByEtablissementIdAndNiveauIdAndSerieId(Long etablissementId, Long niveauId, Long serieId);
    Optional<MatiereNiveauSerie> findByEtablissementIdAndMatiereIdAndNiveauIdAndSerieId(
        Long etablissementId, Long matiereId, Long niveauId, Long serieId);
}
