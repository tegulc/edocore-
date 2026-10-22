package com.educore.modules.cantine.controller;

import com.educore.modules.cantine.dto.CantineDto;
import com.educore.modules.cantine.service.CantineService;
import com.educore.security.UtilisateurPrincipal;
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
@RequestMapping("/api/v1")
@Tag(name = "Cantine", description = "Menus, abonnements, pointage des repas")
public class CantineController {

    private final CantineService cantineService;

    public CantineController(CantineService cantineService) {
        this.cantineService = cantineService;
    }

    @GetMapping("/menus-cantine")
    public ResponseEntity<List<CantineDto.MenuProfil>> menus(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(cantineService.menusDeLaSemaine(principal.etablissementId(), debut, fin));
    }

    @PostMapping("/menus-cantine")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME')")
    public ResponseEntity<CantineDto.MenuProfil> planifier(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody CantineDto.MenuCreation requete) {
        return ResponseEntity.ok(cantineService.planifierMenu(principal.etablissementId(), requete));
    }

    @PostMapping("/eleves/{eleveId}/abonnement-cantine")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'COMPTABLE')")
    public ResponseEntity<CantineDto.AbonnementProfil> abonner(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                  @PathVariable Long eleveId, @Valid @RequestBody CantineDto.AbonnementCreation requete) {
        return ResponseEntity.ok(cantineService.abonner(principal.etablissementId(), eleveId, requete));
    }

    @PostMapping("/presences-cantine")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'SURVEILLANT_GENERAL')")
    public ResponseEntity<CantineDto.PresenceProfil> pointer(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody CantineDto.PresenceCreation requete) {
        return ResponseEntity.ok(cantineService.pointer(principal.etablissementId(), requete));
    }
}
