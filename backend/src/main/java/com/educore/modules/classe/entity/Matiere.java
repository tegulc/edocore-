package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "matieres", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "code"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Matiere implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String libelle;
    private String couleur = "#3498db";
    @Column(name = "type_matiere", length = 30) private String typeMatiere; // GENERALE, LANGUE_VIVANTE, OPTION, SPECIALITE
    @Column(name = "langue_vivante_niveau") private Short langueVivanteNiveau; // 1, 2, 3 pour LV1, LV2, LV3
    @Column(name = "is_option") private Boolean isOption = false;
    @Column(columnDefinition = "TEXT") private String description;
}
