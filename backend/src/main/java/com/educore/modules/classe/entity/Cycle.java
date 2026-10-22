package com.educore.modules.classe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "cycles")
public class Cycle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 20) private String code; // PREMIER_CYCLE, SECOND_CYCLE
    @Column(nullable = false, length = 50) private String libelle;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false) private Short dureeAnnees;
}
