package com.educore.modules.infirmerie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "vaccins")
public class Vaccin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "nom_vaccin", nullable = false) private String nomVaccin;
    @Column(name = "date_administration", nullable = false) private LocalDate dateAdministration;
    @Column(name = "rappel_prevu") private LocalDate rappelPrevu;
}
