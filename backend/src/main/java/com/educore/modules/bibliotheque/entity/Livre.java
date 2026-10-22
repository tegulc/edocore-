package com.educore.modules.bibliotheque.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "livres")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Livre implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    private String isbn;
    @Column(nullable = false) private String titre;
    private String auteur;
    private String editeur;
    private String categorie;
    @Column(name = "nombre_exemplaires", nullable = false) private Integer nombreExemplaires = 1;
    @Column(name = "exemplaires_disponibles", nullable = false) private Integer exemplairesDisponibles = 1;
    @Column(name = "qr_code", unique = true) private String qrCode;
    @Column(name = "couverture_url") private String couvertureUrl;
}
