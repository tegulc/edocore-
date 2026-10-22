package com.educore.modules.infirmerie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/** Pas de colonne etablissement_id propre (rattachee via eleve_id). */
@Getter @Setter
@Entity
@Table(name = "consultations_medicales")
public class ConsultationMedicale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "date_consultation", nullable = false) private Instant dateConsultation = Instant.now();
    @Column(nullable = false) private String motif;
    private String diagnostic;
    private String traitement;
    @Column(name = "infirmier_id") private Long infirmierId;
}
