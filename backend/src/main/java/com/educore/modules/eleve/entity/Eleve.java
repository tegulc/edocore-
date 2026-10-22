package com.educore.modules.eleve.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "eleves", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "matricule"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Eleve implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, updatable = false) private UUID uuid = UUID.randomUUID();
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "utilisateur_id") private Long utilisateurId; // nullable : pas tous les eleves n'ont un compte de connexion
    @Column(nullable = false) private String matricule;
    @Column(nullable = false) private String nom;
    @Column(nullable = false) private String prenom;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 1) private String sexe; // M, F
    @Column(name = "date_naissance", nullable = false) private LocalDate dateNaissance;
    @Column(name = "lieu_naissance") private String lieuNaissance;
    private String nationalite;
    private String religion;
    @Column(name = "groupe_sanguin") private String groupeSanguin;
    private String allergies;
    @Column(name = "photo_url") private String photoUrl;
    @Column(name = "acte_naissance_url") private String acteNaissanceUrl;
    @Column(name = "certificat_medical_url") private String certificatMedicalUrl;
    @Column(name = "qr_code", unique = true) private String qrCode;
    private String statut = "actif"; // actif, transfere, exclu, diplome, abandonne
    @Column(name = "date_inscription", nullable = false) private LocalDate dateInscription = LocalDate.now();
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
}
