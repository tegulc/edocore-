package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "salles", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "nom"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Salle implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String nom;
    @Column(nullable = false) private Integer capacite;
    @Column(name = "type_salle") private String typeSalle = "classe"; // classe, labo, informatique, sport
}
