package com.educore.modules.pedagogie.controller;

import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.pedagogie.dto.PedagogieDto;
import com.educore.modules.pedagogie.service.DevoirService;
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
@Tag(name = "Devoirs", description = "Module 11 — Gestion des devoirs")
public class DevoirController {

    private final DevoirService devoirService;
    private final EleveService eleveService;

    public DevoirController(DevoirService devoirService, EleveService eleveService) {
        this.devoirService = devoirService;
        this.eleveService = eleveService;
    }

    @PostMapping("/devoirs")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<PedagogieDto.DevoirProfil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @Valid @RequestBody PedagogieDto.DevoirCreation requete) {
        return ResponseEntity.ok(devoirService.creer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/classes-matiere/{classeMatiereId}/devoirs")
    public ResponseEntity<List<PedagogieDto.DevoirProfil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                   @PathVariable Long classeMatiereId) {
        return ResponseEntity.ok(devoirService.lister(principal.etablissementId(), classeMatiereId));
    }

    @GetMapping("/devoirs/{id}")
    public ResponseEntity<PedagogieDto.DevoirProfil> obtenir(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(devoirService.obtenir(principal.etablissementId(), id));
    }

    @PostMapping("/devoirs/rendus")
    @PreAuthorize("hasRole('ELEVE')")
    public ResponseEntity<PedagogieDto.DevoirRenduProfil> rendre(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                    @Valid @RequestBody PedagogieDto.DevoirRenduCreation requete) {
        Long eleveId = eleveService.obtenirIdParUtilisateurId(principal.etablissementId(), principal.utilisateurId());
        return ResponseEntity.ok(devoirService.rendre(principal.etablissementId(), eleveId, requete));
    }

    @PatchMapping("/devoirs/rendus/{renduId}/corriger")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<PedagogieDto.DevoirRenduProfil> corriger(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                     @PathVariable Long renduId,
                                                                     @Valid @RequestBody PedagogieDto.DevoirCorrection requete) {
        return ResponseEntity.ok(devoirService.corriger(principal.etablissementId(), principal.utilisateurId(), renduId, requete));
    }

    @GetMapping("/devoirs/{devoirId}/rendus")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<List<PedagogieDto.DevoirRenduProfil>> rendus(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                        @PathVariable Long devoirId) {
        return ResponseEntity.ok(devoirService.rendus(principal.etablissementId(), devoirId));
    }

    @GetMapping("/eleves/{eleveId}/devoirs-rendus")
    public ResponseEntity<List<PedagogieDto.DevoirRenduProfil>> rendusEleve(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                              @PathVariable Long eleveId) {
        return ResponseEntity.ok(devoirService.mesRendus(principal.etablissementId(), eleveId));
    }
}
