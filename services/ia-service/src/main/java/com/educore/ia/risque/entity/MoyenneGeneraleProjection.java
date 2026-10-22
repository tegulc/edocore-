package com.educore.ia.risque.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

/** Projection en LECTURE SEULE sur moyennes_generales (calculee par educore-backend, Module 8). */
@Getter
@Entity
@Table(name = "moyennes_generales")
public class MoyenneGeneraleProjection {
    @Id private Long id;
    @Column(name = "eleve_id") private Long eleveId;
    @Column(name = "classe_id") private Long classeId;
    @Column(name = "periode_id") private Long periodeId;
    @Column(name = "moyenne_generale") private BigDecimal moyenneGenerale;
}
