package com.educore.modules.cantine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "abonnements_cantine")
public class AbonnementCantine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(nullable = false) private BigDecimal montant;
    @Column(name = "statut_paiement") private String statutPaiement = "impaye";
}
