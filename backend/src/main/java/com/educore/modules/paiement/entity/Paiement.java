package com.educore.modules.paiement.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "paiements")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Paiement implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, updatable = false) private UUID uuid = UUID.randomUUID();
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "tranche_id") private Long trancheId;
    @Column(name = "moyen_paiement_id", nullable = false) private Long moyenPaiementId;
    @Column(nullable = false) private BigDecimal montant;
    @Column(name = "reference_transaction") private String referenceTransaction;
    @Column(nullable = false) private String statut = "en_attente"; // en_attente, confirme, echoue, rembourse
    @Column(name = "date_paiement", nullable = false) private Instant datePaiement = Instant.now();
    @Column(name = "encaisse_par_id") private Long encaisseParId;
}
