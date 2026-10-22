package com.educore.modules.paiement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

/** Pas de colonne etablissement_id propre (rattache via eleve_id). */
@Getter @Setter
@Entity
@Table(name = "echeanciers_paiement")
public class EcheancierPaiement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "structure_frais_id", nullable = false) private Long structureFraisId;
    @Column(name = "montant_total", nullable = false) private BigDecimal montantTotal;
    @Column(name = "nombre_tranches", nullable = false) private Integer nombreTranches = 1;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
