package com.educore.modules.etablissement.repository;

import com.educore.modules.etablissement.entity.EtablissementModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtablissementModuleRepository extends JpaRepository<EtablissementModule, Long> {
    List<EtablissementModule> findByEtablissementId(Long etablissementId);
    Optional<EtablissementModule> findByEtablissementIdAndModuleCode(Long etablissementId, String moduleCode);
}
