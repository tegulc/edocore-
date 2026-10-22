package com.educore.modules.pedagogie.controller;

import com.educore.modules.pedagogie.dto.PedagogieDto;
import com.educore.modules.pedagogie.service.CahierDeTexteService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cahier de texte", description = "Module 10 — Cahier de texte numerique")
public class CahierDeTexteController {

    private final CahierDeTexteService cahierService;

    public CahierDeTexteController(CahierDeTexteService cahierService) {
        this.cahierService = cahierService;
    }

    @PostMapping("/cahier-de-texte")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<PedagogieDto.CahierProfil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @Valid @RequestBody PedagogieDto.CahierCreation requete) {
        return ResponseEntity.ok(cahierService.creer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/classes-matiere/{classeMatiereId}/cahier-de-texte")
    public ResponseEntity<List<PedagogieDto.CahierProfil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                   @PathVariable Long classeMatiereId) {
        return ResponseEntity.ok(cahierService.lister(principal.etablissementId(), classeMatiereId));
    }

    @GetMapping("/cahier-de-texte/{id}")
    public ResponseEntity<PedagogieDto.CahierProfil> obtenir(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(cahierService.obtenir(principal.etablissementId(), id));
    }
}
