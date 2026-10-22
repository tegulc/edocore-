package com.educore.modules.note.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Devoir/sequence/composition/examen donnant lieu a des notes. Workflow :
 * planifiee -> saisie (notes entrees) -> validee (verrouillee, ne peut plus
 * etre modifiee) -> publiee (visible eleves/parents).
 */
@Getter @Setter
@Entity
@Table(name = "evaluations")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Evaluation implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "classe_matiere_id", nullable = false) private Long classeMatiereId;
    @Column(name = "type_evaluation_id", nullable = false) private Long typeEvaluationId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(nullable = false) private String titre;
    @Column(name = "date_evaluation", nullable = false) private LocalDate dateEvaluation;
    @Column(nullable = false) private BigDecimal bareme = BigDecimal.valueOf(20);
    private String statut = "planifiee"; // planifiee, saisie, validee, publiee
    @Column(name = "created_by_id") private Long createdById;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
