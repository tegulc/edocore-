package com.educore.modules.cantine.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "menus_cantine")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class MenuCantine implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "date_jour", nullable = false) private LocalDate dateJour;
    @Column(name = "libelle_plat", nullable = false) private String libellePlat;
    @Column(name = "informations_nutritionnelles") private String informationsNutritionnelles;
}
