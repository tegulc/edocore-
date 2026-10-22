package com.educore.modules.etablissement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Etablissement : racine du multi-tenant. Toutes les autres donnees de la
 * plateforme sont rattachees, directement ou indirectement, a un etablissement
 * (voir document d'architecture, Partie 7 - Architecture multi-tenant).
 *
 * Volontairement N'implemente PAS TenantScoped : un etablissement n'appartient
 * pas a un autre tenant, il EST le tenant. Sa propre isolation (RLS + filtre
 * applicatif) compare son id directement, voir migration V2 et EtablissementService.
 */
@Getter
@Setter
@Entity
@Table(name = "etablissements")
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String sigle;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private String pays;

    private String ville;

    private String adresse;

    private String telephone;

    private String email;

    @Column(name = "type_etablissement")
    private String typeEtablissement; // primaire, college, lycee, universite

    @Enumerated(EnumType.STRING)
    private StatutEtablissement statut = StatutEtablissement.ACTIF;

    @Column(name = "plan_abonnement")
    private String planAbonnement = "essai"; // essai, standard, premium

    @Column(name = "date_debut_abonnement")
    private LocalDate dateDebutAbonnement;

    @Column(name = "date_fin_abonnement")
    private LocalDate dateFinAbonnement;

    private String devise = "XAF";

    @Column(name = "langue_defaut")
    private String langueDefaut = "fr";

    @Column(name = "fuseau_horaire")
    private String fuseauHoraire = "Africa/Douala";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum StatutEtablissement { ACTIF, SUSPENDU, RESILIE }
}
