package com.educore.modules.infirmerie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "urgences_medicales")
public class UrgenceMedicale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "date_urgence", nullable = false) private Instant dateUrgence = Instant.now();
    @Column(nullable = false) private String description;
    @Column(name = "action_prise") private String actionPrise;
    @Column(name = "hopital_transfert") private String hopitalTransfert;
    @Column(name = "parent_notifie") private boolean parentNotifie = false;
}
