package com.educore.notifications.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "destinataire_id", nullable = false) private Long destinataireId;
    @Column(nullable = false) private String categorie; // paiement, retard, absence, reunion, notes, discipline, autre
    @Column(nullable = false) private String titre;
    private String contenu;
    @Column(nullable = false) private String canal; // sms, email, whatsapp, push
    @Column(name = "statut_envoi") private String statutEnvoi = "en_attente";
    @Column(name = "envoye_le") private Instant envoyeLe;
    @Column(name = "lu_le") private Instant luLe;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
