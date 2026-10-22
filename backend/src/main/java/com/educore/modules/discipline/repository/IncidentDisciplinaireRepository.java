package com.educore.modules.discipline.repository;

import com.educore.modules.discipline.entity.IncidentDisciplinaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentDisciplinaireRepository extends JpaRepository<IncidentDisciplinaire, Long> {
    List<IncidentDisciplinaire> findByEleveIdOrderByDateIncidentDesc(Long eleveId);
}
