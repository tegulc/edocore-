package com.educore.ia.assistant.controller;

import com.educore.ia.assistant.dto.AssistantDto;
import com.educore.ia.assistant.service.AssistantService;
import com.educore.ia.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant conversationnel", description = "Questions/reponses (non connecte a un LLM reel dans ce sandbox)")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/questions")
    public ResponseEntity<AssistantDto.Echange> poser(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody AssistantDto.Question requete) {
        return ResponseEntity.ok(assistantService.poserQuestion(principal.utilisateurId(), requete));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<AssistantDto.Echange>> historique(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(assistantService.historique(principal.utilisateurId()));
    }
}
