package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
    Optional<Cycle> findByCode(String code);
}
