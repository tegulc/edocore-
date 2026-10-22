package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.StructureFrais;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StructureFraisRepository extends JpaRepository<StructureFrais, Long> {
    List<StructureFrais> findByEtablissementIdAndAnneeScolaireIdAndNiveauId(Long etablissementId, Long anneeScolaireId, Long niveauId);
}
