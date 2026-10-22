package com.educore.modules.reunion.repository;

import com.educore.modules.reunion.entity.ReunionInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReunionInviteRepository extends JpaRepository<ReunionInvite, Long> {
    List<ReunionInvite> findByReunionId(Long reunionId);
    Optional<ReunionInvite> findByReunionIdAndUtilisateurId(Long reunionId, Long utilisateurId);
}
