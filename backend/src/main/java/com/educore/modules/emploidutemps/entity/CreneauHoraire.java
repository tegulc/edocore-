package com.educore.modules.emploidutemps.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalTime;

@Getter @Setter
@Entity
@Table(name = "creneaux_horaires")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class CreneauHoraire implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String libelle; // "07h30-08h25"
    @Column(name = "heure_debut", nullable = false) private LocalTime heureDebut;
    @Column(name = "heure_fin", nullable = false) private LocalTime heureFin;
    @Column(nullable = false) private Short ordre;
}
