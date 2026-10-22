package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ClasseRepository extends JpaRepository<Classe, Long> {
    List<Classe> findByEtablissementIdAndAnneeScolaireIdOrderByLibelleAsc(Long etablissementId, Long anneeScolaireId);
    boolean existsByEtablissementIdAndAnneeScolaireIdAndLibelle(Long etablissementId, Long anneeScolaireId, String libelle);

    /** Le module Eleves (Phase 3b) n'est pas encore modelise en JPA ; la table 'inscriptions' existe deja en SQL. */
    @Query(value = "SELECT COUNT(*) FROM inscriptions WHERE classe_id = :classeId AND statut = 'validee'", nativeQuery = true)
    long compterElevesInscrits(Long classeId);
}
