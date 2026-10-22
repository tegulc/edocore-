package com.educore.modules.reunion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "comptes_rendus")
public class CompteRendu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "reunion_id", nullable = false) private Long reunionId;
    private String contenu;
    @Column(name = "fichier_url") private String fichierUrl;
    @Column(name = "redige_par_id") private Long redigeParId;
}
