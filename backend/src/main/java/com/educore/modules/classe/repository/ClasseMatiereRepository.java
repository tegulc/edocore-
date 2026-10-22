package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.ClasseMatiere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClasseMatiereRepository extends JpaRepository<ClasseMatiere, Long> {
    List<ClasseMatiere> findByClasseId(Long classeId);
    Optional<ClasseMatiere> findByClasseIdAndMatiereId(Long classeId, Long matiereId);
    List<ClasseMatiere> findByEnseignantId(Long enseignantId);
}
