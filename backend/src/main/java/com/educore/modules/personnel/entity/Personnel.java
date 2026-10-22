package com.educore.modules.personnel.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "personnel")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Personnel implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    private String matricule;
    private String fonction;
    private String departement;
    @Column(name = "date_embauche") private LocalDate dateEmbauche;
    @Column(name = "type_contrat") private String typeContrat;
    @Column(name = "salaire_base") private BigDecimal salaireBase;
    @Column(nullable = false) private String statut = "actif";
}
