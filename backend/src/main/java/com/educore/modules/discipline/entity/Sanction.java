package com.educore.modules.discipline.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "sanctions")
public class Sanction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "incident_id") private Long incidentId;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "type_sanction_id", nullable = false) private Long typeSanctionId;
    @Column(name = "date_sanction", nullable = false) private LocalDate dateSanction = LocalDate.now();
    @Column(name = "duree_jours") private Integer dureeJours;
    private String motif;
    @Column(name = "decidee_par_id") private Long decideeParId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
