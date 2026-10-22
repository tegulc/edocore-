package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "series", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "code"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Serie implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String code; // A, C, D, Espagnol...
    @Column(name = "sous_serie_code") private String sousSerieCode; // A1, A2, A3, etc.
    @Column(nullable = false) private String libelle;
    @Column(name = "serie_parent_id") private Long serieParentId;
    @Column(name = "sous_systeme_id") private Long sousSystemeId;
    @Column(length = 100) private String orientation; // LITTERAIRE, SCIENTIFIQUE, TECHNOLOGIQUE, BILINGUE
    @Column(columnDefinition = "TEXT") private String description;
}
