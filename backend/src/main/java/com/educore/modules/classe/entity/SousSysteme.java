package com.educore.modules.classe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "sous_systemes")
public class SousSysteme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 20) private String code; // FRANCOPHONE, ANGLOPHONE, BILINGUE
    @Column(nullable = false, length = 50) private String libelle;
    @Column(columnDefinition = "TEXT") private String description;
}
