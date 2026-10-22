package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Pas de colonne etablissement_id propre (rattachee via evaluation_id, cf. RLS). */
@Getter @Setter
@Entity
@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {"evaluation_id", "eleve_id"}))
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "evaluation_id", nullable = false) private Long evaluationId;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    private BigDecimal valeur;
    private boolean absent = false;
    private String observation;
    @Column(name = "saisi_par_id") private Long saisiParId;
    private boolean valide = false;
    @Column(name = "valide_par_id") private Long valideParId;
    @Column(name = "date_saisie", nullable = false) private Instant dateSaisie = Instant.now();
}
