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
 * Journal d'audit des tentatives de connexion (succes ET echecs).
 * Ecriture seule (append-only) : aucune methode de mise a jour ou de
 * suppression n'est exposee (principe P5 - Auditabilite totale, voir
 * document d'architecture section 8.5).
 */
@Getter
@Setter
@Entity
@Table(name = "historique_connexions")
public class HistoriqueConnexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(nullable = false)
    private boolean succes;

    @Column(name = "date_tentative", nullable = false)
    private Instant dateTentative = Instant.now();
}
