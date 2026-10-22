package com.educore.modules.etablissement.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Annee scolaire (ex: "2025-2026"). Regle metier centrale : une seule annee
 * peut etre "active" a la fois par etablissement - voir AnneeScolaireService.activer(),
 * qui desactive automatiquement toute autre annee active du meme etablissement.
 * Tout le systeme (notes, presences, paiements...) filtre par defaut sur
 * l'annee active plutot que de forcer chaque module a le redemander.
 */
@Getter
@Setter
@Entity
@Table(name = "annees_scolaires", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "libelle"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class AnneeScolaire implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etablissement_id", nullable = false)
    private Long etablissementId;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "est_active", nullable = false)
    private boolean estActive = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
