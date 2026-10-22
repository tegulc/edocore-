package com.educore.modules.paiement.controller;

import com.educore.common.exception.BusinessException;
import com.educore.modules.paiement.dto.PaiementDto;
import com.educore.modules.paiement.service.PaiementService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/paiements")
@Tag(name = "Paiements", description = "Paiement mobile money (initiation asynchrone) et encaissement manuel")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping("/initier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'COMPTABLE', 'PARENT')")
    @Operation(summary = "Initie un paiement mobile money. En-tete Idempotency-Key obligatoire (Volume 6, Partie IV.3).")
    public ResponseEntity<PaiementDto.InitiationReponse> initier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                    @RequestHeader(value = "Idempotency-Key", required = false) String cleIdempotence,
                                                                    @Valid @RequestBody PaiementDto.Initiation requete) {
        if (cleIdempotence == null || cleIdempotence.isBlank()) {
            throw new BusinessException("IDEMPOTENCY_KEY_MANQUANTE", "L'en-tete Idempotency-Key est obligatoire pour initier un paiement.", HttpStatus.BAD_REQUEST);
        }
        var reponse = paiementService.initier(principal.etablissementId(), cleIdempotence, requete);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reponse);
    }

    @PostMapping("/encaisser")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'COMPTABLE')")
    @Operation(summary = "Enregistre un encaissement immediat au guichet (especes, cheque) - confirme directement")
    public ResponseEntity<PaiementDto.Profil> encaisser(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                          @Valid @RequestBody PaiementDto.Encaissement requete) {
        return ResponseEntity.ok(paiementService.encaisser(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/eleves/{eleveId}")
    @Operation(summary = "Historique des paiements d'un eleve")
    public ResponseEntity<List<PaiementDto.Profil>> historique(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(paiementService.historique(principal.etablissementId(), eleveId));
    }
}
