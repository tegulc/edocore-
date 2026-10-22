package com.educore.modules.rapport.controller;

import com.educore.modules.rapport.dto.RapportDto;
import com.educore.modules.rapport.service.RapportService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rapports")
@Tag(name = "Rapports", description = "Module 27 — Rapports statistiques")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'COMPTABLE', 'ECONOME')")
public class RapportController {

    private final RapportService rapportService;

    public RapportController(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    @GetMapping("/synthese")
    public ResponseEntity<RapportDto.Synthese> synthese(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(rapportService.synthese(principal.etablissementId()));
    }

    @GetMapping("/effectifs-classes")
    public ResponseEntity<List<RapportDto.EffectifClasse>> effectifs(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(rapportService.effectifsParClasse(principal.etablissementId()));
    }

    @GetMapping("/soldes-eleves")
    public ResponseEntity<List<RapportDto.SoldeEleve>> soldes(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(rapportService.soldesEleves(principal.etablissementId()));
    }

    @GetMapping("/taux-presences")
    public ResponseEntity<List<RapportDto.TauxPresenceEleve>> presences(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(rapportService.tauxPresences(principal.etablissementId()));
    }

    @GetMapping("/moyennes-classe")
    public ResponseEntity<List<RapportDto.MoyenneClasse>> moyennes(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                    @RequestParam Long classeId,
                                                                    @RequestParam Long periodeId) {
        return ResponseEntity.ok(rapportService.moyennesClasse(principal.etablissementId(), classeId, periodeId));
    }
}
