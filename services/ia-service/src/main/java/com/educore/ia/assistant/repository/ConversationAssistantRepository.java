package com.educore.ia.assistant.repository;

import com.educore.ia.assistant.entity.ConversationAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationAssistantRepository extends JpaRepository<ConversationAssistant, Long> {
    List<ConversationAssistant> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);
}
