package com.educore.modules.note.repository;

import com.educore.modules.note.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByClasseMatiereIdAndPeriodeId(Long classeMatiereId, Long periodeId);
    List<Evaluation> findByPeriodeIdAndStatut(Long periodeId, String statut);
}
