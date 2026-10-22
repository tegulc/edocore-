package com.educore.modules.comptabilite.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "comptes_comptables", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "code"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class CompteComptable implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String libelle;
    @Column(name = "type_compte", nullable = false) private String typeCompte; // actif, passif, charge, produit
}
