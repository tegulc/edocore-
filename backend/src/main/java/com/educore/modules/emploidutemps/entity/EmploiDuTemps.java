package com.educore.modules.emploidutemps.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/**
 * Seance hebdomadaire recurrente (classe+matiere+enseignant+creneau+jour, pour
 * une annee scolaire). Les contraintes anti-conflit (prof/salle/classe jamais
 * double-reserves sur le meme creneau) sont deja imposees en base par des
 * index uniques (voir V1) ; EmploiDuTempsService les revalide en amont pour
 * offrir un message d'erreur clair plutot qu'une violation SQL brute.
 */
@Getter @Setter
@Entity
@Table(name = "emploi_du_temps")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class EmploiDuTemps implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "classe_id", nullable = false) private Long classeId;
    @Column(name = "matiere_id", nullable = false) private Long matiereId;
    @Column(name = "enseignant_id", nullable = false) private Long enseignantId;
    @Column(name = "salle_id") private Long salleId;
    @Column(name = "creneau_id", nullable = false) private Long creneauId;
    @Column(name = "jour_semaine", nullable = false) private Short jourSemaine; // 1=lundi ... 7=dimanche
    @Column(name = "annee_scolaire_id", nullable = false) private Long anneeScolaireId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
