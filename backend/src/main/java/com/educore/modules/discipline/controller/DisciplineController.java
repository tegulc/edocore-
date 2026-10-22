package com.educore.modules.discipline.controller;

import com.educore.modules.discipline.dto.DisciplineDto;
import com.educore.modules.discipline.service.DisciplineService;
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
@Tag(name = "Discipline", description = "Incidents disciplinaires et sanctions")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL')")
public class DisciplineController {

    private final DisciplineService disciplineService;

    public DisciplineController(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    @PostMapping("/incidents-disciplinaires")
    public ResponseEntity<DisciplineDto.IncidentProfil> signaler(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                    @Valid @RequestBody DisciplineDto.IncidentCreation requete) {
        return ResponseEntity.ok(disciplineService.signaler(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/eleves/{eleveId}/incidents-disciplinaires")
    public ResponseEntity<List<DisciplineDto.IncidentProfil>> incidents(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(disciplineService.historiqueIncidents(principal.etablissementId(), eleveId));
    }

    @PostMapping("/sanctions")
    public ResponseEntity<DisciplineDto.SanctionProfil> sanctionner(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                       @Valid @RequestBody DisciplineDto.SanctionCreation requete) {
        return ResponseEntity.ok(disciplineService.sanctionner(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/eleves/{eleveId}/sanctions")
    public ResponseEntity<List<DisciplineDto.SanctionProfil>> sanctions(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(disciplineService.historiqueSanctions(principal.etablissementId(), eleveId));
    }
}
