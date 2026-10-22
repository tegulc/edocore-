package com.educore.modules.presence.controller;

import com.educore.modules.presence.dto.PresenceDto;
import com.educore.modules.presence.service.PresenceService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/presences")
@Tag(name = "Presences", description = "Pointage et suivi de presence des eleves")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL', 'PROFESSEUR')")
    @Operation(summary = "Enregistre (ou met a jour) un pointage de presence")
    public ResponseEntity<PresenceDto.Profil> enregistrer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                             @Valid @RequestBody PresenceDto.Enregistrement requete) {
        return ResponseEntity.ok(presenceService.enregistrer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @PatchMapping("/{id}/justifier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL')")
    public ResponseEntity<PresenceDto.Profil> justifier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                          @PathVariable Long id, @Valid @RequestBody PresenceDto.Justification requete) {
        return ResponseEntity.ok(presenceService.justifier(principal.etablissementId(), id, requete));
    }

    @GetMapping("/eleves/{eleveId}")
    @Operation(summary = "Historique de presence d'un eleve sur une periode")
    public ResponseEntity<List<PresenceDto.Profil>> historique(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                  @PathVariable Long eleveId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(presenceService.historique(principal.etablissementId(), eleveId, debut, fin));
    }

    @GetMapping("/eleves/{eleveId}/bilan")
    @Operation(summary = "Nombre d'absences non justifiees et de retards sur une periode")
    public ResponseEntity<PresenceDto.Bilan> bilan(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                     @PathVariable Long eleveId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(presenceService.bilan(principal.etablissementId(), eleveId, debut, fin));
    }
}
