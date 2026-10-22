package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "niveaux", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "code"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Niveau implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String code; // 6e, 5e, 4e, 3e, 2nde, 1ere, Tle
    @Column(nullable = false) private String libelle;
    @Column(nullable = false) private Short ordre;
    private String cycle; // premier_cycle, second_cycle
}
