package com.educore.modules.comptabilite.controller;

import com.educore.modules.comptabilite.dto.EcritureComptableDto;
import com.educore.modules.comptabilite.service.EcritureComptableService;
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
@RequestMapping("/api/v1/ecritures-comptables")
@Tag(name = "Comptabilite", description = "Journal comptable")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'COMPTABLE')")
public class EcritureComptableController {

    private final EcritureComptableService ecritureComptableService;

    public EcritureComptableController(EcritureComptableService ecritureComptableService) {
        this.ecritureComptableService = ecritureComptableService;
    }

    @GetMapping
    public ResponseEntity<List<EcritureComptableDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(ecritureComptableService.lister(principal.etablissementId(), debut, fin));
    }

    @PostMapping
    public ResponseEntity<EcritureComptableDto.Profil> enregistrer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                      @Valid @RequestBody EcritureComptableDto.Creation requete) {
        return ResponseEntity.ok(ecritureComptableService.enregistrer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/balance")
    @Operation(summary = "Balance comptable (total debit/credit/solde par compte) sur une periode")
    public ResponseEntity<List<EcritureComptableDto.Balance>> balance(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(ecritureComptableService.balance(principal.etablissementId(), debut, fin));
    }
}
