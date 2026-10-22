package com.educore.modules.parent.repository;

import com.educore.modules.parent.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    List<Parent> findByEtablissementId(Long etablissementId);
    Optional<Parent> findByUtilisateurId(Long utilisateurId);
}
