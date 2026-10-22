package com.educore.modules.discipline.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

/** Pas de colonne etablissement_id propre (rattache via eleve_id). */
@Getter @Setter
@Entity
@Table(name = "incidents_disciplinaires")
public class IncidentDisciplinaire {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "date_incident", nullable = false) private LocalDate dateIncident;
    @Column(nullable = false) private String description;
    @Column(name = "signale_par_id") private Long signaleParId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
