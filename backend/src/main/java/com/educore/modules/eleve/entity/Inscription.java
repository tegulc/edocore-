package com.educore.modules.eleve.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Une ligne par annee scolaire = reinscription geree naturellement (commentaire
 * du script SQL original). Pas de colonne etablissement_id propre : isolation
 * assuree via classe_id -> classes.etablissement_id (voir migration RLS).
 */
@Getter @Setter
@Entity
@Table(name = "inscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "annee_scolaire_id"}))
public class Inscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "annee_scolaire_id", nullable = false) private Long anneeScolaireId;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "type_inscription", nullable = false) private String typeInscription = "inscription"; // inscription, reinscription, transfert
    @Column(name = "date_inscription", nullable = false) private LocalDate dateInscription = LocalDate.now();
    @Column(nullable = false) private String statut = "validee"; // en_attente, validee, annulee
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
