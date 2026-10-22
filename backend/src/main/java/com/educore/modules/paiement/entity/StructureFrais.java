package com.educore.modules.paiement.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "structures_frais")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class StructureFrais implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "annee_scolaire_id", nullable = false) private Long anneeScolaireId;
    @Column(name = "niveau_id", nullable = false) private Long niveauId;
    @Column(nullable = false) private String libelle;
    @Column(nullable = false) private BigDecimal montant;
    private boolean obligatoire = true;
}
