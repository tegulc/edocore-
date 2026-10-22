package com.educore.modules.reunion.repository;

import com.educore.modules.reunion.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByEtablissementIdOrderByDateReunionDesc(Long etablissementId);
}
