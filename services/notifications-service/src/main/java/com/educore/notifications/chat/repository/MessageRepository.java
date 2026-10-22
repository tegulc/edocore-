package com.educore.notifications.chat.repository;

import com.educore.notifications.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByEnvoyeLeAsc(Long conversationId);
}
