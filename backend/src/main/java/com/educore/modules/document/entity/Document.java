package com.educore.modules.document.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "documents")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Document implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "proprietaire_type", nullable = false) private String proprietaireType;
    @Column(name = "proprietaire_id", nullable = false) private Long proprietaireId;
    @Column(name = "type_document", nullable = false) private String typeDocument;
    @Column(nullable = false) private String titre;
    @Column(name = "fichier_url", nullable = false) private String fichierUrl;
    private Boolean archive = false;
    @Column(name = "date_ajout", nullable = false) private Instant dateAjout = Instant.now();
    @Column(name = "ajoute_par_id") private Long ajouteParId;
}
