package com.educore.modules.etablissement.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "jours_feries")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class JourFerie implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etablissement_id", nullable = false)
    private Long etablissementId;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(name = "recurrent_annuel")
    private boolean recurrentAnnuel = false;
}
