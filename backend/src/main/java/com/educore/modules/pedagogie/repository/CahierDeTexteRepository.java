package com.educore.modules.pedagogie.repository;

import com.educore.modules.pedagogie.entity.CahierDeTexte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CahierDeTexteRepository extends JpaRepository<CahierDeTexte, Long> {
    List<CahierDeTexte> findByClasseMatiereIdOrderByDateCoursDesc(Long classeMatiereId);
}
