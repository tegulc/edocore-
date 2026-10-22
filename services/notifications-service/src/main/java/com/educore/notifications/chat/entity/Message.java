package com.educore.notifications.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "conversation_id", nullable = false) private Long conversationId;
    @Column(name = "expediteur_id", nullable = false) private Long expediteurId;
    private String contenu;
    @Column(name = "type_contenu") private String typeContenu = "texte";
    @Column(name = "fichier_url") private String fichierUrl;
    private boolean lu = false;
    @Column(name = "envoye_le", nullable = false) private Instant envoyeLe = Instant.now();
}
