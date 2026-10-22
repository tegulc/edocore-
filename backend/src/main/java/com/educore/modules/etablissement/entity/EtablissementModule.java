package com.educore.modules.etablissement.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/**
 * Active/desactive un module fonctionnel pour un etablissement donne (cahier
 * des charges, 3.1 Administrateur : "Parametrer les modules actifs pour
 * l'etablissement"). Table absente du script SQL fourni initialement, ajoutee
 * ici (migration V3) car explicitement requise par le cahier des charges et
 * necessaire au principe P2 (modularite activable) du document d'architecture.
 */
@Getter
@Setter
@Entity
@Table(name = "etablissement_modules", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "module_code"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class EtablissementModule implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etablissement_id", nullable = false)
    private Long etablissementId;

    @Column(name = "module_code", nullable = false)
    private String moduleCode; // ELEVES, NOTES, PAIEMENTS, BIBLIOTHEQUE, TRANSPORT, CANTINE, IA...

    @Column(nullable = false)
    private boolean actif = false;

    @Column(name = "date_maj", nullable = false)
    private Instant dateMaj = Instant.now();
}
