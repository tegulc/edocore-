package com.educore.modules.reunion.repository;

import com.educore.modules.reunion.entity.CompteRendu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompteRenduRepository extends JpaRepository<CompteRendu, Long> {
    Optional<CompteRendu> findByReunionId(Long reunionId);
}
