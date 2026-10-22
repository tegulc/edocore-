package com.educore.notifications.chat.repository;

import com.educore.notifications.chat.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    List<ConversationParticipant> findByConversationId(Long conversationId);
    List<ConversationParticipant> findByUtilisateurId(Long utilisateurId);
    Optional<ConversationParticipant> findByConversationIdAndUtilisateurId(Long conversationId, Long utilisateurId);
}
