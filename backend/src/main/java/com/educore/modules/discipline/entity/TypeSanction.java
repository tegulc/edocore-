package com.educore.modules.discipline.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Referentiel global (comme types_evaluation) - seede en migration. */
@Getter @Setter
@Entity
@Table(name = "types_sanction")
public class TypeSanction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String code; // avertissement, blame, exclusion_temp, exclusion_def
    @Column(nullable = false) private String libelle;
    private Short gravite = 1;
}
