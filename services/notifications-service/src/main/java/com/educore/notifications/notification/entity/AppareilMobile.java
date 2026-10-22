package com.educore.notifications.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "appareils_mobiles")
public class AppareilMobile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(nullable = false) private String plateforme; // android, ios
    @Column(name = "push_token", nullable = false) private String pushToken;
    @Column(name = "date_enregistrement", nullable = false) private Instant dateEnregistrement = Instant.now();
}
