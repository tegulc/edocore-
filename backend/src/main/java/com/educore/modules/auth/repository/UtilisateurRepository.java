package com.educore.modules.auth.repository;

import com.educore.modules.auth.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmailAndEtablissementId(String email, Long etablissementId);

    Optional<Utilisateur> findByEmail(String email); // utilise a la connexion, avant resolution du tenant

    Page<Utilisateur> findByEtablissementId(Long etablissementId, Pageable pageable);
}
