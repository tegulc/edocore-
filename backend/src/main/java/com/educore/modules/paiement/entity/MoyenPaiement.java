package com.educore.modules.paiement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Referentiel global (comme types_evaluation) - seede en migration. */
@Getter @Setter
@Entity
@Table(name = "moyens_paiement")
public class MoyenPaiement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String code; // orange_money, mtn_momo, carte_bancaire, virement, especes
}
