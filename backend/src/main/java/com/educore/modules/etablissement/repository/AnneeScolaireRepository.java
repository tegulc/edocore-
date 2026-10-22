package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {

    List<AnneeScolaire> findByEtablissementIdOrderByDateDebutDesc(Long etablissementId);

    Optional<AnneeScolaire> findByEtablissementIdAndEstActiveTrue(Long etablissementId);

    boolean existsByEtablissementIdAndLibelle(Long etablissementId, String libelle);

    @Modifying
    @Query("UPDATE AnneeScolaire a SET a.estActive = false WHERE a.etablissementId = :etablissementId AND a.estActive = true")
    void desactiverToutesLesAnnees(Long etablissementId);
}
