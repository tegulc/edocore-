package com.educore.notifications.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ChatDto {

    public record CreationConversation(String titre, @NotEmpty List<Long> participantIds) {}

    public record ConversationProfil(Long id, String typeConversation, String titre, List<Long> participantIds) {}

    public record EnvoiMessage(@NotNull String contenu, String typeContenu, String fichierUrl) {}

    public record MessageProfil(Long id, Long conversationId, Long expediteurId, String contenu,
                                 String typeContenu, String fichierUrl, boolean lu, String envoyeLe) {}
}
