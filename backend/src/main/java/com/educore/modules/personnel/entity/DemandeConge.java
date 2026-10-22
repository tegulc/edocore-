package com.educore.modules.personnel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "demandes_conges")
public class DemandeConge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(name = "type_conge", nullable = false) private String typeConge;
    @Column(name = "date_debut", nullable = false) private LocalDate dateDebut;
    @Column(name = "date_fin", nullable = false) private LocalDate dateFin;
    private String motif;
    @Column(nullable = false) private String statut = "en_attente";
    @Column(name = "valide_par_id") private Long valideParId;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
