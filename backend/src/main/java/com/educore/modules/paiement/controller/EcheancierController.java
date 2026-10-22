package com.educore.modules.paiement.controller;

import com.educore.modules.paiement.dto.EcheancierDto;
import com.educore.modules.paiement.service.EcheancierService;
import com.educore.security.UtilisateurPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eleves/{eleveId}/echeanciers")
@Tag(name = "Echeanciers", description = "Echeanciers de paiement des eleves")
public class EcheancierController {

    private final EcheancierService echeancierService;

    public EcheancierController(EcheancierService echeancierService) {
        this.echeancierService = echeancierService;
    }

    @GetMapping
    public ResponseEntity<List<EcheancierDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(echeancierService.listerPourEleve(principal.etablissementId(), eleveId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'COMPTABLE')")
    public ResponseEntity<EcheancierDto.Profil> generer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                          @PathVariable Long eleveId, @Valid @RequestBody EcheancierDto.Generation requete) {
        return ResponseEntity.ok(echeancierService.genererPourEleve(principal.etablissementId(), eleveId, requete));
    }
}
