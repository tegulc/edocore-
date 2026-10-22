package com.educore.modules.discipline.repository;

import com.educore.modules.discipline.entity.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SanctionRepository extends JpaRepository<Sanction, Long> {
    List<Sanction> findByEleveIdOrderByDateSanctionDesc(Long eleveId);
}
