package com.educore.modules.etablissement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Periode pedagogique (trimestre, sequence ou semestre selon le systeme
 * choisi par l'etablissement). Rattachee a une annee scolaire, pas directement
 * a un etablissement (pas de colonne etablissement_id propre - isolation
 * assuree par sous-requete, voir migration V2 et Periode n'implemente donc
 * pas TenantScoped au sens strict de l'interface).
 */
@Getter
@Setter
@Entity
@Table(name = "periodes", uniqueConstraints = @UniqueConstraint(columnNames = {"annee_scolaire_id", "type_periode", "numero"}))
public class Periode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annee_scolaire_id", nullable = false)
    private Long anneeScolaireId;

    @Column(name = "type_periode", nullable = false)
    private String typePeriode; // trimestre, sequence, semestre

    @Column(nullable = false)
    private Short numero;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "est_cloturee", nullable = false)
    private boolean estCloturee = false;
}
