package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "moyennes_matiere", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "classe_matiere_id", "periode_id"}))
public class MoyenneMatiere {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "classe_matiere_id", nullable = false) private Long classeMatiereId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    private BigDecimal moyenne;
    @Column(name = "rang_matiere") private Integer rangMatiere;
    private String appreciation;
    @Column(name = "calcule_le", nullable = false) private Instant calculeLe = Instant.now();
}
