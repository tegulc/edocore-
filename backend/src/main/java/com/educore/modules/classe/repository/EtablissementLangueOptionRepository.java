package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.EtablissementLangueOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtablissementLangueOptionRepository extends JpaRepository<EtablissementLangueOption, Long> {
    List<EtablissementLangueOption> findByEtablissementId(Long etablissementId);
    List<EtablissementLangueOption> findByEtablissementIdAndTypeOffre(Long etablissementId, String typeOffre);
}
