package com.educore.notifications.chat.controller;

import com.educore.notifications.chat.dto.ChatDto;
import com.educore.notifications.chat.service.ChatService;
import com.educore.notifications.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Chat", description = "Conversations et messagerie instantanee")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatDto.ConversationProfil>> mesConversations(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(chatService.mesConversations(principal.utilisateurId()));
    }

    @PostMapping
    public ResponseEntity<ChatDto.ConversationProfil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                              @Valid @RequestBody ChatDto.CreationConversation requete) {
        return ResponseEntity.ok(chatService.creerConversation(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatDto.MessageProfil>> historique(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(chatService.historique(id, principal.utilisateurId()));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ChatDto.MessageProfil> envoyer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                           @PathVariable Long id, @Valid @RequestBody ChatDto.EnvoiMessage requete) {
        return ResponseEntity.ok(chatService.envoyerMessage(id, principal.utilisateurId(), requete));
    }
}
