package com.educore.notifications.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "type_conversation", nullable = false) private String typeConversation = "prive"; // prive, groupe
    private String titre;
    @Column(name = "created_by_id") private Long createdById;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
