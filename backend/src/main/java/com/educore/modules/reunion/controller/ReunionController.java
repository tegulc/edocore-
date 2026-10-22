package com.educore.modules.reunion.controller;

import com.educore.modules.reunion.dto.ReunionDto;
import com.educore.modules.reunion.service.ReunionService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reunions")
@Tag(name = "Reunions", description = "Planification, invitations, comptes-rendus")
public class ReunionController {

    private final ReunionService reunionService;

    public ReunionController(ReunionService reunionService) {
        this.reunionService = reunionService;
    }

    @GetMapping
    public ResponseEntity<List<ReunionDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(reunionService.lister(principal.etablissementId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<ReunionDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody ReunionDto.Creation requete) {
        return ResponseEntity.ok(reunionService.creer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/{id}/invites")
    public ResponseEntity<List<ReunionDto.InviteProfil>> invites(@PathVariable Long id) {
        return ResponseEntity.ok(reunionService.invites(id));
    }

    @PatchMapping("/{id}/reponse")
    public ResponseEntity<ReunionDto.InviteProfil> repondre(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                             @PathVariable Long id, @Valid @RequestBody ReunionDto.Reponse requete) {
        return ResponseEntity.ok(reunionService.repondre(id, principal.utilisateurId(), requete));
    }

    @PutMapping("/{id}/compte-rendu")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<ReunionDto.CompteRenduProfil> rediger(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                  @PathVariable Long id, @Valid @RequestBody ReunionDto.CompteRenduCreation requete) {
        return ResponseEntity.ok(reunionService.rediger(id, principal.utilisateurId(), requete));
    }
}
