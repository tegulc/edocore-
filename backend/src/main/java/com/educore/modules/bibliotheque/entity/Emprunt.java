package com.educore.modules.bibliotheque.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/** Pas de colonne etablissement_id propre (rattache via livre_id). */
@Getter @Setter
@Entity
@Table(name = "emprunts")
public class Emprunt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "livre_id", nullable = false) private Long livreId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(name = "date_emprunt", nullable = false) private LocalDate dateEmprunt = LocalDate.now();
    @Column(name = "date_retour_prevue", nullable = false) private LocalDate dateRetourPrevue;
    @Column(name = "date_retour_effective") private LocalDate dateRetourEffective;
    private String statut = "en_cours"; // en_cours, retourne, perdu, en_retard
}
