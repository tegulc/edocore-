package com.educore.modules.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Session utilisateur : une ligne par refresh token emis. Permet la
 * revocation individuelle (deconnexion, compromission suspectee) sans
 * attendre l'expiration naturelle du jeton.
 * Correspond a la table utilisateur_sessions du script SQL fourni.
 */
@Getter
@Setter
@Entity
@Table(name = "utilisateur_sessions")
public class UtilisateurSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash; // le refresh token brut n'est jamais stocke, seulement son hash (SHA-256)

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "date_connexion", nullable = false)
    private Instant dateConnexion = Instant.now();

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Column(nullable = false)
    private boolean revoque = false;
}
