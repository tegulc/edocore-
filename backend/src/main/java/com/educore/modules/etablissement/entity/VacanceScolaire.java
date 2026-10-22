package com.educore.modules.etablissement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Rattachee a une annee scolaire (pas de colonne etablissement_id propre, voir Periode). */
@Getter
@Setter
@Entity
@Table(name = "vacances_scolaires")
public class VacanceScolaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annee_scolaire_id", nullable = false)
    private Long anneeScolaireId;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
}
