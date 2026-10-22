package com.educore.notifications.chat.service;

import com.educore.notifications.chat.dto.ChatDto;
import com.educore.notifications.chat.entity.Conversation;
import com.educore.notifications.chat.entity.ConversationParticipant;
import com.educore.notifications.chat.entity.Message;
import com.educore.notifications.chat.repository.ConversationParticipantRepository;
import com.educore.notifications.chat.repository.ConversationRepository;
import com.educore.notifications.chat.repository.MessageRepository;
import com.educore.notifications.common.RessourceIntrouvableException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Chat (Module 22). Validation des participants volontairement legere : ce
 * service, separe du backend principal, ne rappelle pas educore-backend pour
 * verifier chaque participant (couplage reseau synchrone evite par design -
 * voir README du service). La RLS reste le filet de securite en cas d'id errone.
 * Regle 01 : seul un participant de la conversation peut y ecrire/lire.
 */
@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ConversationRepository conversationRepository, ConversationParticipantRepository participantRepository,
                        MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ChatDto.ConversationProfil creerConversation(Long etablissementId, Long createurId, ChatDto.CreationConversation requete) {
        Conversation conversation = new Conversation();
        conversation.setEtablissementId(etablissementId);
        conversation.setTypeConversation(requete.participantIds().size() > 1 ? "groupe" : "prive");
        conversation.setTitre(requete.titre());
        conversation.setCreatedById(createurId);
        conversationRepository.save(conversation);

        ajouterParticipant(conversation.getId(), createurId);
        requete.participantIds().forEach(id -> {
            if (!id.equals(createurId)) ajouterParticipant(conversation.getId(), id);
        });

        return versProfilConversation(conversation);
    }

    public List<ChatDto.ConversationProfil> mesConversations(Long utilisateurId) {
        return participantRepository.findByUtilisateurId(utilisateurId).stream()
                .map(p -> conversationRepository.findById(p.getConversationId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::versProfilConversation)
                .toList();
    }

    @Transactional
    public ChatDto.MessageProfil envoyerMessage(Long conversationId, Long expediteurId, ChatDto.EnvoiMessage requete) {
        exigerParticipant(conversationId, expediteurId);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setExpediteurId(expediteurId);
        message.setContenu(requete.contenu());
        if (requete.typeContenu() != null) message.setTypeContenu(requete.typeContenu());
        message.setFichierUrl(requete.fichierUrl());
        messageRepository.save(message);

        ChatDto.MessageProfil profil = versProfilMessage(message);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, profil);
        return profil;
    }

    public List<ChatDto.MessageProfil> historique(Long conversationId, Long demandeurId) {
        exigerParticipant(conversationId, demandeurId);
        return messageRepository.findByConversationIdOrderByEnvoyeLeAsc(conversationId).stream().map(this::versProfilMessage).toList();
    }

    private void exigerParticipant(Long conversationId, Long utilisateurId) {
        conversationRepository.findById(conversationId).orElseThrow(() -> new RessourceIntrouvableException("Conversation", conversationId));
        participantRepository.findByConversationIdAndUtilisateurId(conversationId, utilisateurId)
                .orElseThrow(() -> new AccessDeniedException("Vous ne participez pas a cette conversation."));
    }

    private void ajouterParticipant(Long conversationId, Long utilisateurId) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversationId(conversationId);
        participant.setUtilisateurId(utilisateurId);
        participantRepository.save(participant);
    }

    private ChatDto.ConversationProfil versProfilConversation(Conversation c) {
        List<Long> participantIds = participantRepository.findByConversationId(c.getId()).stream()
                .map(ConversationParticipant::getUtilisateurId).toList();
        return new ChatDto.ConversationProfil(c.getId(), c.getTypeConversation(), c.getTitre(), participantIds);
    }

    private ChatDto.MessageProfil versProfilMessage(Message m) {
        return new ChatDto.MessageProfil(m.getId(), m.getConversationId(), m.getExpediteurId(), m.getContenu(),
                m.getTypeContenu(), m.getFichierUrl(), m.isLu(), DateTimeFormatter.ISO_INSTANT.format(m.getEnvoyeLe()));
    }
}
