package com.educore.notifications.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "conversation_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "utilisateur_id"}))
public class ConversationParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "conversation_id", nullable = false) private Long conversationId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(name = "date_ajout", nullable = false) private Instant dateAjout = Instant.now();
}
