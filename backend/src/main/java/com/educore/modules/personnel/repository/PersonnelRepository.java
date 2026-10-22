package com.educore.modules.personnel.repository;

import com.educore.modules.personnel.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
    List<Personnel> findByEtablissementIdOrderByMatriculeAsc(Long etablissementId);
    Optional<Personnel> findByEtablissementIdAndUtilisateurId(Long etablissementId, Long utilisateurId);
}
