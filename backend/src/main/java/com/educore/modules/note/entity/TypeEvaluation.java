package com.educore.modules.note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/** Referentiel global (pas de colonne etablissement_id, comme roles/permissions - deja seede en V1). */
@Getter @Setter
@Entity
@Table(name = "types_evaluation")
public class TypeEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String libelle;
    @Column(nullable = false) private BigDecimal ponderation;
}
