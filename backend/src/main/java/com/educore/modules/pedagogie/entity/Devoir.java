package com.educore.modules.pedagogie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "devoirs")
public class Devoir {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "classe_matiere_id", nullable = false) private Long classeMatiereId;
    @Column(nullable = false) private String titre;
    private String description;
    @Column(name = "date_publication", nullable = false) private LocalDate datePublication = LocalDate.now();
    @Column(name = "date_limite", nullable = false) private LocalDate dateLimite;
    @Column(name = "fichier_sujet_url") private String fichierSujetUrl;
    @Column(name = "created_by_id") private Long createdById;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
