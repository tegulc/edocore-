package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Tableau d'honneur individuel - généré automatiquement à partir des bulletins
 * Un élève y figure si sa moyenne générale >= 12/20 et le bulletin est publié
 */
@Getter @Setter
@Entity
@Table(name = "tableaux_honneur", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "periode_id"}))
public class TableauHonneur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(name = "moyenne_generale", nullable = false) private BigDecimal moyenneGenerale;
    private Integer rang;
    @Column(name = "mention") private String mention; // Excellent, Très bien, Bien, Assez bien
    @Column(name = "bulletin_id") private Long bulletinId;
    @Column(name = "genere_le") private Instant genereLe;
    @Column(name = "publie_le") private Instant publieLe;
    private String statut = "actif"; // actif, archive
}
