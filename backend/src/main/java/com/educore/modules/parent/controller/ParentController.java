package com.educore.modules.parent.controller;

import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.parent.dto.ParentDto;
import com.educore.modules.parent.service.ParentService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parents")
@Tag(name = "Parents", description = "Gestion des comptes parents/tuteurs")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping
    public ResponseEntity<List<ParentDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(parentService.lister(principal.etablissementId()));
    }

    @GetMapping("/moi/enfants")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Liste les eleves rattaches au parent connecte")
    public ResponseEntity<List<EleveDto.Profil>> mesEnfants(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(parentService.listerMesEnfants(principal.etablissementId(), principal.utilisateurId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE')")
    @Operation(summary = "Cree un compte parent (compte utilisateur + profil)")
    public ResponseEntity<ParentDto.CreationReponse> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                             @Valid @RequestBody ParentDto.Creation requete) {
        return ResponseEntity.ok(parentService.creer(principal.etablissementId(), requete));
    }
}
