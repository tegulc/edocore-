package com.educore.modules.emploidutemps.repository;

import com.educore.modules.emploidutemps.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {
    List<EmploiDuTemps> findByEtablissementIdAndAnneeScolaireIdAndClasseId(Long etablissementId, Long anneeScolaireId, Long classeId);
    List<EmploiDuTemps> findByEtablissementIdAndAnneeScolaireIdAndEnseignantId(Long etablissementId, Long anneeScolaireId, Long enseignantId);

    boolean existsByEnseignantIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(Long enseignantId, Long creneauId, Short jourSemaine, Long anneeScolaireId);
    boolean existsByClasseIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(Long classeId, Long creneauId, Short jourSemaine, Long anneeScolaireId);
    boolean existsBySalleIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(Long salleId, Long creneauId, Short jourSemaine, Long anneeScolaireId);
}
