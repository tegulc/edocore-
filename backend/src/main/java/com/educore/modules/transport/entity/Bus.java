package com.educore.modules.transport.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "bus")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Bus implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String immatriculation;
    @Column(nullable = false) private Integer capacite;
    @Column(name = "chauffeur_id") private Long chauffeurId;
    @Column(name = "dispositif_gps_id") private String dispositifGpsId;
    private String statut = "actif";
}
