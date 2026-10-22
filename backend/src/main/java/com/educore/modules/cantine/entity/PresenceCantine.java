package com.educore.modules.cantine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "presences_cantine")
public class PresenceCantine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "date_jour", nullable = false) private LocalDate dateJour;
    private String statut = "present";
}
