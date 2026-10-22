package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "bulletins", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "periode_id"}))
public class Bulletin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(name = "moyenne_generale") private BigDecimal moyenneGenerale;
    private Integer rang;
    @Column(name = "appreciation_generale") private String appreciationGenerale;
    @Column(name = "decision_conseil") private String decisionConseil;
    @Column(name = "nombre_absences") private Integer nombreAbsences = 0;
    @Column(name = "fichier_pdf_url") private String fichierPdfUrl;
    private String statut = "brouillon"; // brouillon, valide, publie
    @Column(name = "genere_le") private Instant genereLe;
    
    // Workflow conseil de classe
    @Column(name = "date_conseil") private LocalDate dateConseil;
    @Column(name = "recompenses") private String recompenses; // félicitations, encouragements, tableau d'honneur
    @Column(name = "avertissement_travail") private Boolean avertissementTravail;
    @Column(name = "avertissement_conduite") private Boolean avertissementConduite;
    @Column(name = "blame") private Boolean blame;
    @Column(name = "exclusion") private Boolean exclusion;
    @Column(name = "observation_conseil") private String observationConseil;
    @Column(name = "decision_finale") private String decisionFinale; // passage, redoublement, orientation
    @Column(name = "classe_orientation") private String classeOrientation;
    @Column(name = "valide_par_id") private Long valideParId;
    @Column(name = "date_validation") private Instant dateValidation;
}
