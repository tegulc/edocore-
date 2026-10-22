package com.educore.modules.classe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Affectation d'une matiere a une classe : coefficient, volume horaire, enseignant assigne. */
@Getter @Setter
@Entity
@Table(name = "classe_matiere", uniqueConstraints = @UniqueConstraint(columnNames = {"classe_id", "matiere_id"}))
public class ClasseMatiere {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "matiere_id", nullable = false) private Long matiereId;
    @Column(name = "enseignant_id") private Long enseignantId;
    @Column(nullable = false) private BigDecimal coefficient = BigDecimal.ONE;
    @Column(name = "volume_horaire_hebdo", nullable = false) private BigDecimal volumeHoraireHebdo = BigDecimal.ONE;
}
