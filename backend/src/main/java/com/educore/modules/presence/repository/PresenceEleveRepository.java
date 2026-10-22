package com.educore.modules.presence.repository;

import com.educore.modules.presence.entity.PresenceEleve;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenceEleveRepository extends JpaRepository<PresenceEleve, Long> {
    List<PresenceEleve> findByEleveIdAndDateJourBetweenOrderByDateJourDesc(Long eleveId, LocalDate debut, LocalDate fin);
    Optional<PresenceEleve> findByEleveIdAndClasseMatiereIdAndDateJour(Long eleveId, Long classeMatiereId, LocalDate dateJour);
    List<PresenceEleve> findByClasseMatiereIdAndDateJour(Long classeMatiereId, LocalDate dateJour);
    long countByEleveIdAndTypePresenceAndJustifieFalseAndDateJourBetween(Long eleveId, String typePresence, LocalDate debut, LocalDate fin);

    /** Utilise par le Module Dashboard. Filtre par eleve_id appartenant au tenant via sous-requete (pas de colonne etablissement_id propre). */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT COUNT(*) FROM presences_eleves pe JOIN eleves e ON e.id = pe.eleve_id
        WHERE e.etablissement_id = :etablissementId AND pe.date_jour = :jour""", nativeQuery = true)
    long compterPresencesDuJour(Long etablissementId, LocalDate jour);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT COUNT(*) FROM presences_eleves pe JOIN eleves e ON e.id = pe.eleve_id
        WHERE e.etablissement_id = :etablissementId AND pe.date_jour = :jour AND pe.type_presence = 'present'""", nativeQuery = true)
    long compterPresentsDuJour(Long etablissementId, LocalDate jour);
}
