package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodeRepository extends JpaRepository<Periode, Long> {
    List<Periode> findByAnneeScolaireIdOrderByNumeroAsc(Long anneeScolaireId);
    boolean existsByAnneeScolaireIdAndTypePeriodeAndNumero(Long anneeScolaireId, String typePeriode, Short numero);
}
