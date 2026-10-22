package com.educore.modules.enseignant.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Profil professionnel d'un enseignant, rattache 1-1 a un Utilisateur (role
 * PROFESSEUR). Separe de Utilisateur pour ne jamais polluer le socle
 * d'authentification (Module 1) avec des champs specifiques a un seul role
 * parmi 15 (salaire, specialite, type de contrat...).
 */
@Getter @Setter
@Entity
@Table(name = "enseignants", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "matricule"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Enseignant implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    private String matricule;
    private String specialite;
    @Column(name = "diplome_principal") private String diplomePrincipal;
    @Column(name = "date_embauche") private LocalDate dateEmbauche;
    @Column(name = "type_contrat") private String typeContrat; // permanent, vacataire, contractuel
    @Column(name = "salaire_base") private BigDecimal salaireBase;
    @Column(name = "taux_horaire_supp") private BigDecimal tauxHoraireSupp;
    private String statut = "actif";
}
