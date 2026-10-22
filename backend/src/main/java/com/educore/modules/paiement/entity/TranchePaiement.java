package com.educore.modules.paiement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "tranches_paiement")
public class TranchePaiement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "echeancier_id", nullable = false) private Long echeancierId;
    @Column(name = "numero_tranche", nullable = false) private Integer numeroTranche;
    @Column(name = "montant_du", nullable = false) private BigDecimal montantDu;
    @Column(name = "date_echeance", nullable = false) private LocalDate dateEcheance;
    private String statut = "impaye"; // impaye, partiel, paye, en_retard
}
