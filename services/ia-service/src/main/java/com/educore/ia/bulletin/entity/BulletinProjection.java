package com.educore.ia.bulletin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

/** Projection en LECTURE SEULE sur bulletins (Module 8, educore-backend). */
@Getter
@Entity
@Table(name = "bulletins")
public class BulletinProjection {
    @Id private Long id;
    @Column(name = "eleve_id") private Long eleveId;
    @Column(name = "moyenne_generale") private BigDecimal moyenneGenerale;
    private Integer rang;
    @Column(name = "nombre_absences") private Integer nombreAbsences;
}
