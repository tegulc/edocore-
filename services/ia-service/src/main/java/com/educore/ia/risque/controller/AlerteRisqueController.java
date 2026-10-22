package com.educore.ia.risque.controller;

import com.educore.ia.risque.dto.AlerteRisqueDto;
import com.educore.ia.risque.service.DetectionRisqueService;
import com.educore.ia.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alertes-risque")
@Tag(name = "Detection de risque", description = "Score explicable de risque d'echec, suggestion au conseil de classe")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
public class AlerteRisqueController {

    private final DetectionRisqueService detectionRisqueService;

    public AlerteRisqueController(DetectionRisqueService detectionRisqueService) {
        this.detectionRisqueService = detectionRisqueService;
    }

    @PostMapping("/classes/{classeId}/calculer")
    public ResponseEntity<List<AlerteRisqueDto.Profil>> calculer(@PathVariable Long classeId, @RequestParam Long periodeId,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debutPeriode,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finPeriode) {
        return ResponseEntity.ok(detectionRisqueService.calculerPourClasse(classeId, periodeId, debutPeriode, finPeriode));
    }

    @GetMapping
    public ResponseEntity<List<AlerteRisqueDto.Profil>> elevesARisque(@AuthenticationPrincipal UtilisateurPrincipal principal, @RequestParam Long periodeId) {
        return ResponseEntity.ok(detectionRisqueService.elevesARisque(periodeId));
    }
}
