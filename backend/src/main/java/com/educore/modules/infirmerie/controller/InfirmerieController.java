package com.educore.modules.infirmerie.controller;

import com.educore.modules.infirmerie.dto.InfirmerieDto;
import com.educore.modules.infirmerie.service.InfirmerieService;
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
@Tag(name = "Infirmerie", description = "Consultations, urgences medicales, vaccins")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'INFIRMIER')")
public class InfirmerieController {

    private final InfirmerieService infirmerieService;

    public InfirmerieController(InfirmerieService infirmerieService) {
        this.infirmerieService = infirmerieService;
    }

    @PostMapping("/consultations-medicales")
    public ResponseEntity<InfirmerieDto.ConsultationProfil> consulter(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                         @Valid @RequestBody InfirmerieDto.ConsultationCreation requete) {
        return ResponseEntity.ok(infirmerieService.consulter(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/eleves/{eleveId}/consultations-medicales")
    public ResponseEntity<List<InfirmerieDto.ConsultationProfil>> consultations(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(infirmerieService.historiqueConsultations(principal.etablissementId(), eleveId));
    }

    @PostMapping("/urgences-medicales")
    public ResponseEntity<InfirmerieDto.UrgenceProfil> declarerUrgence(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                          @Valid @RequestBody InfirmerieDto.UrgenceCreation requete) {
        return ResponseEntity.ok(infirmerieService.declarerUrgence(principal.etablissementId(), requete));
    }

    @GetMapping("/eleves/{eleveId}/urgences-medicales")
    public ResponseEntity<List<InfirmerieDto.UrgenceProfil>> urgences(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(infirmerieService.historiqueUrgences(principal.etablissementId(), eleveId));
    }

    @PostMapping("/vaccins")
    public ResponseEntity<InfirmerieDto.VaccinProfil> enregistrerVaccin(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                           @Valid @RequestBody InfirmerieDto.VaccinCreation requete) {
        return ResponseEntity.ok(infirmerieService.enregistrerVaccin(principal.etablissementId(), requete));
    }

    @GetMapping("/eleves/{eleveId}/vaccins")
    public ResponseEntity<List<InfirmerieDto.VaccinProfil>> vaccins(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long eleveId) {
        return ResponseEntity.ok(infirmerieService.historiqueVaccins(principal.etablissementId(), eleveId));
    }
}
