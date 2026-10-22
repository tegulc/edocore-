package com.educore.modules.comptabilite.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "journal_comptable")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class EcritureComptable implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "compte_id", nullable = false) private Long compteId;
    @Column(name = "date_ecriture", nullable = false) private LocalDate dateEcriture;
    @Column(nullable = false) private String libelle;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    @Column(name = "piece_justificative_url") private String pieceJustificativeUrl;
    @Column(name = "reference_source") private String referenceSource; // ex: "paiement:1234"
    @Column(name = "saisi_par_id") private Long saisiParId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
