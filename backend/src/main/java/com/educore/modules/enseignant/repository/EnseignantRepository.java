package com.educore.modules.enseignant.repository;

import com.educore.modules.enseignant.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {
    List<Enseignant> findByEtablissementId(Long etablissementId);
    Optional<Enseignant> findByUtilisateurId(Long utilisateurId);
}
