package com.educore.ia.assistant.service;

import com.educore.ia.assistant.dto.AssistantDto;
import com.educore.ia.assistant.entity.ConversationAssistant;
import com.educore.ia.assistant.repository.ConversationAssistantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Assistant conversationnel (Module 25). AUCUN modele de langage reel connecte
 * dans ce sandbox (pas d'acces reseau externe ni de cle API). Chaque question
 * est journalisee et recoit une reponse fixe et transparente sur cette
 * limite, plutot que d'inventer une reponse qui semblerait credible sans
 * l'etre (principe de precaution : mieux vaut un stub honnete qu'une
 * simulation trompeuse). A brancher sur un vrai fournisseur LLM en production.
 */
@Service
public class AssistantService {

    private final ConversationAssistantRepository conversationRepository;

    public AssistantService(ConversationAssistantRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public AssistantDto.Echange poserQuestion(Long utilisateurId, AssistantDto.Question requete) {
        ConversationAssistant conversation = new ConversationAssistant();
        conversation.setUtilisateurId(utilisateurId);
        conversation.setQuestion(requete.question());
        // TODO production : appeler un fournisseur LLM (ex: Claude, GPT) avec le contexte
        // pertinent (role de l'utilisateur, donnees scolaires autorisees) et journaliser la reponse reelle.
        conversation.setReponse("Assistant IA non encore connecte a un fournisseur reel dans cet environnement. Votre question a ete enregistree.");
        conversationRepository.save(conversation);
        return versEchange(conversation);
    }

    public List<AssistantDto.Echange> historique(Long utilisateurId) {
        return conversationRepository.findByUtilisateurIdOrderByCreatedAtDesc(utilisateurId).stream().map(this::versEchange).toList();
    }

    private AssistantDto.Echange versEchange(ConversationAssistant c) {
        return new AssistantDto.Echange(c.getId(), c.getQuestion(), c.getReponse(), DateTimeFormatter.ISO_INSTANT.format(c.getCreatedAt()));
    }
}
