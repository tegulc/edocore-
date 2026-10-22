package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.VacanceScolaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacanceScolaireRepository extends JpaRepository<VacanceScolaire, Long> {
    List<VacanceScolaire> findByAnneeScolaireIdOrderByDateDebutAsc(Long anneeScolaireId);
}
