package com.educore.modules.note.repository;

import com.educore.modules.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByEvaluationId(Long evaluationId);
    Optional<Note> findByEvaluationIdAndEleveId(Long evaluationId, Long eleveId);

    @org.springframework.data.jpa.repository.Query("""
        SELECT n FROM Note n WHERE n.eleveId = :eleveId AND n.valide = true
        AND n.evaluationId IN (SELECT e.id FROM Evaluation e WHERE e.classeMatiereId = :classeMatiereId AND e.periodeId = :periodeId)
        """)
    List<Note> trouverNotesValideesPourMatierePeriode(Long eleveId, Long classeMatiereId, Long periodeId);
}
