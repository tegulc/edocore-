package com.educore.modules.eleve.repository;

import com.educore.modules.eleve.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    Optional<Inscription> findByEleveIdAndAnneeScolaireId(Long eleveId, Long anneeScolaireId);
    List<Inscription> findByClasseIdAndStatut(Long classeId, String statut);
    List<Inscription> findByEleveIdOrderByAnneeScolaireIdDesc(Long eleveId);
}
