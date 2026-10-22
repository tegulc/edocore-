package com.educore.ia.assistant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "ia_conversations_assistant")
public class ConversationAssistant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(nullable = false) private String question;
    private String reponse;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
