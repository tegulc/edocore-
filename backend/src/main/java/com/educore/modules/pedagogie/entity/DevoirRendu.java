package com.educore.modules.pedagogie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "devoirs_rendus", uniqueConstraints = @UniqueConstraint(columnNames = {"devoir_id", "eleve_id"}))
public class DevoirRendu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "devoir_id", nullable = false) private Long devoirId;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "fichier_reponse_url") private String fichierReponseUrl;
    @Column(name = "date_soumission", nullable = false) private Instant dateSoumission = Instant.now();
    private BigDecimal note;
    @Column(name = "correction_commentaire") private String correctionCommentaire;
    @Column(name = "corrige_par_id") private Long corrigeParId;
    @Column(name = "corrige_le") private Instant corrigeLe;
    @Column(nullable = false) private String statut = "rendu";
}
