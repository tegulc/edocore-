package com.educore.modules.pedagogie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "cahier_de_texte")
public class CahierDeTexte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "classe_matiere_id", nullable = false) private Long classeMatiereId;
    @Column(name = "date_cours", nullable = false) private LocalDate dateCours;
    private String chapitre;
    private String lecon;
    private String contenu;
    private String exercices;
    @Column(name = "created_by_id") private Long createdById;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
