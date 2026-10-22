package com.educore.modules.presence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Pointage de presence d'un eleve. classeMatiereId nullable = pointage general
 * d'entree d'etablissement (portail/QR code) plutot qu'un cours specifique.
 * Pas de colonne etablissement_id propre (isolation via eleve_id, cf. RLS).
 */
@Getter @Setter
@Entity
@Table(name = "presences_eleves")
public class PresenceEleve {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "classe_matiere_id") private Long classeMatiereId;
    @Column(name = "date_jour", nullable = false) private LocalDate dateJour;
    @Column(name = "heure_pointage") private LocalTime heurePointage;
    @Column(name = "type_presence", nullable = false) private String typePresence = "present"; // present, absent, retard, exclu_cours
    @Column(name = "methode_pointage") private String methodePointage; // qrcode, nfc, empreinte, faciale, manuel
    private boolean justifie = false;
    @Column(name = "motif_justification") private String motifJustification;
    @Column(name = "enregistre_par_id") private Long enregistreParId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
