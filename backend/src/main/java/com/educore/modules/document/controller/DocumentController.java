package com.educore.modules.document.controller;

import com.educore.modules.document.dto.DocumentDto;
import com.educore.modules.document.service.DocumentService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Module 21 — Gestion documentaire")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'SECRETAIRE', 'CENSEUR')")
    public ResponseEntity<DocumentDto.Profil> archiver(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                        @Valid @RequestBody DocumentDto.Creation requete) {
        return ResponseEntity.ok(documentService.archiver(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                              @RequestParam(required = false) String proprietaireType,
                                                              @RequestParam(required = false) Long proprietaireId) {
        return ResponseEntity.ok(documentService.lister(principal.etablissementId(), proprietaireType, proprietaireId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto.Profil> obtenir(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(documentService.obtenir(principal.etablissementId(), id));
    }
}
