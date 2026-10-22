package com.educore.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Journal d'audit transversal des ACTIONS (creation, modification, suppression,
 * export...), complementaire a historique_connexions (module auth) qui ne
 * couvre que les tentatives de connexion. Cahier des charges Module 1 :
 * "historique des connexions et des actions".
 *
 * Ecriture seule (append-only) — voir JournalAuditRepository : aucune methode
 * de mise a jour/suppression n'est exposee.
 *
 * Utilise par TOUS les modules (transversal, cf. document d'architecture
 * section 3.3) via JournalAuditService, jamais directement.
 */
@Getter
@Setter
@Entity
@Table(name = "journal_audit")
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etablissement_id")
    private Long etablissementId;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, EXPORT...

    @Column(name = "table_cible")
    private String tableCible;

    @Column(name = "enregistrement_id")
    private Long enregistrementId;

    @Column(name = "anciennes_valeurs", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String anciennesValeurs;

    @Column(name = "nouvelles_valeurs", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String nouvellesValeurs;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(nullable = false)
    private Instant horodatage = Instant.now();
}
