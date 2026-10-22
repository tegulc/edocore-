package com.educore.modules.eleve.repository;

import com.educore.modules.eleve.entity.Eleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EleveRepository extends JpaRepository<Eleve, Long> {
    Page<Eleve> findByEtablissementId(Long etablissementId, Pageable pageable);
    boolean existsByEtablissementIdAndMatricule(Long etablissementId, String matricule);
    Optional<Eleve> findByEtablissementIdAndQrCode(Long etablissementId, String qrCode);
    long countByEtablissementIdAndStatut(Long etablissementId, String statut);
    Optional<Eleve> findByEtablissementIdAndUtilisateurId(Long etablissementId, Long utilisateurId);
}
