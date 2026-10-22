package com.educore.modules.auth.repository;

import com.educore.modules.auth.entity.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {

    List<UtilisateurRole> findByUtilisateurId(Long utilisateurId);

    Optional<UtilisateurRole> findByUtilisateurIdAndEstPrincipalTrue(Long utilisateurId);

    boolean existsByUtilisateurIdAndRoleId(Long utilisateurId, Long roleId);

    void deleteByUtilisateurIdAndRoleId(Long utilisateurId, Long roleId);

    void deleteByUtilisateurId(Long utilisateurId);

    @Modifying
    @Query("UPDATE UtilisateurRole ur SET ur.estPrincipal = false WHERE ur.utilisateurId = :utilisateurId")
    void retirerPrincipalDeTousLesRoles(Long utilisateurId);

    long countByUtilisateurId(Long utilisateurId);
}
