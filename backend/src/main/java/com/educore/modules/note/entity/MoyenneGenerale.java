package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "moyennes_generales", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "periode_id"}))
public class MoyenneGenerale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(name = "moyenne_generale") private BigDecimal moyenneGenerale;
    @Column(name = "rang_classe") private Integer rangClasse;
    private String decision; // felicitations, encouragements, avertissement, blame
    @Column(name = "calcule_le", nullable = false) private Instant calculeLe = Instant.now();
}
