package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {
    boolean existsByEmail(String email);
}
