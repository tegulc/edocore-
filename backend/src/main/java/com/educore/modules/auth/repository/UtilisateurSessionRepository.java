package com.educore.modules.auth.repository;

import com.educore.modules.auth.entity.UtilisateurSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UtilisateurSessionRepository extends JpaRepository<UtilisateurSession, Long> {

    Optional<UtilisateurSession> findByTokenHashAndRevoqueFalse(String tokenHash);

    @Modifying
    @Query("UPDATE UtilisateurSession s SET s.revoque = true WHERE s.tokenHash = :tokenHash")
    void revoquerParTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE UtilisateurSession s SET s.revoque = true WHERE s.utilisateurId = :utilisateurId")
    void revoquerToutesLesSessions(Long utilisateurId);
}
