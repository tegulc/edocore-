package com.educore.modules.pedagogie.repository;

import com.educore.modules.pedagogie.entity.Devoir;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevoirRepository extends JpaRepository<Devoir, Long> {
    List<Devoir> findByClasseMatiereIdOrderByDateLimiteDesc(Long classeMatiereId);
}
