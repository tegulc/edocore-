package com.educore.modules.comptabilite.controller;

import com.educore.modules.comptabilite.dto.CompteComptableDto;
import com.educore.modules.comptabilite.service.CompteComptableService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comptes-comptables")
@Tag(name = "Comptabilite", description = "Plan comptable et journal en partie double")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'COMPTABLE')")
public class CompteComptableController {

    private final CompteComptableService compteComptableService;

    public CompteComptableController(CompteComptableService compteComptableService) {
        this.compteComptableService = compteComptableService;
    }

    @GetMapping
    public ResponseEntity<List<CompteComptableDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(compteComptableService.lister(principal.etablissementId()));
    }

    @PostMapping
    public ResponseEntity<CompteComptableDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                             @Valid @RequestBody CompteComptableDto.Creation requete) {
        return ResponseEntity.ok(compteComptableService.creer(principal.etablissementId(), requete));
    }
}
