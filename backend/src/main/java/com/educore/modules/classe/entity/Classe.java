package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "classes", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "annee_scolaire_id", "libelle"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Classe implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "annee_scolaire_id", nullable = false) private Long anneeScolaireId;
    @Column(name = "niveau_id", nullable = false) private Long niveauId;
    @Column(name = "serie_id") private Long serieId;
    @Column(nullable = false) private String libelle; // ex: "3eme A1"
    @Column(name = "salle_id") private Long salleId;
    @Column(name = "professeur_principal_id") private Long professeurPrincipalId;
    @Column(nullable = false) private Integer capacite = 60;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
