package com.educore.modules.note.repository;

import com.educore.modules.note.entity.TypeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeEvaluationRepository extends JpaRepository<TypeEvaluation, Long> {
    Optional<TypeEvaluation> findByCode(String code);
}
