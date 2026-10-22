package com.educore.ia.risque.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entité fictive utilisée uniquement pour satisfaire le contrat générique de JpaRepository.
 * Les méthodes du repository utilisent des native queries et ne retournent que des scalaires (long).
 * Cette entité n'a pas de table correspondante en base de données.
 */
@Getter @Setter
@Entity
@Table(name = "ia_indicateurs_risque_temp")  // Table fictive, jamais utilisée
public class IndicateurRisque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
