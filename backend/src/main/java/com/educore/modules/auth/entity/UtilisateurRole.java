package com.educore.modules.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Association utilisateur <-> role (RBAC multi-role, cf. cahier des charges
 * Module 1 : "attribution d'un role unique ou de roles multiples par
 * utilisateur"). estPrincipal designe le role utilise par defaut comme
 * contexte JWT/UI ; toutes les permissions de tous les roles attribues
 * restent neanmoins cumulees pour l'autorisation (voir PermissionService).
 */
@Getter
@Setter
@Entity
@Table(name = "utilisateur_roles", uniqueConstraints = @UniqueConstraint(columnNames = {"utilisateur_id", "role_id"}))
public class UtilisateurRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "est_principal", nullable = false)
    private boolean estPrincipal = false;

    @Column(name = "date_attribution", nullable = false)
    private Instant dateAttribution = Instant.now();
}
