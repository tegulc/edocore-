package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.SousSysteme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SousSystemeRepository extends JpaRepository<SousSysteme, Long> {
    Optional<SousSysteme> findByCode(String code);
}
