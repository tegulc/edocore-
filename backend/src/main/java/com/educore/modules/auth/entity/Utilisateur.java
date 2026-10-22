package com.educore.modules.auth.entity;

import com.educore.common.base.BaseEntity;
import com.educore.common.base.TenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/**
 * Entite Utilisateur : socle de tous les roles (Module 1 du cahier des charges).
 * Correspond a la table `utilisateurs` du script SQL fourni.
 */
@Getter
@Setter
@Entity
@Table(name = "utilisateurs", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "email"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Utilisateur extends BaseEntity implements TenantScoped {

    @Column(name = "etablissement_id", nullable = false)
    private Long etablissementId;

    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String email;

    private String telephone;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    /** Politique d'expiration configurable (cahier des charges 4.3 / Module 1). Null = jamais expire (ex: comptes systeme). */
    @Column(name = "mot_de_passe_expire_le")
    private Instant motDePasseExpireLe;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "langue_preferee")
    private String languePreferee = "fr";

    @Enumerated(EnumType.STRING)
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;

    @Column(name = "deux_fa_active")
    private boolean deuxFaActive = false;

    @Column(name = "deux_fa_secret")
    private String deuxFaSecret;

    @Column(name = "derniere_connexion")
    private Instant derniereConnexion;

    @Column(name = "doit_changer_mdp")
    private boolean doitChangerMdp = false;

    public enum StatutUtilisateur { ACTIF, INACTIF, SUSPENDU, ARCHIVE }
}
