package com.educore.ia.bulletin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "ia_commentaires_bulletin")
public class CommentaireBulletin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "bulletin_id", nullable = false) private Long bulletinId;
    @Column(name = "commentaire_genere", nullable = false) private String commentaireGenere;
    @Column(name = "valide_par_enseignant") private boolean valideParEnseignant = false;
}
