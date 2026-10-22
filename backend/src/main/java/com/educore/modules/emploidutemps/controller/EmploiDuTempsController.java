package com.educore.modules.emploidutemps.controller;

import com.educore.modules.emploidutemps.dto.CreneauHoraireDto;
import com.educore.modules.emploidutemps.dto.EmploiDuTempsDto;
import com.educore.modules.emploidutemps.service.CreneauHoraireService;
import com.educore.modules.emploidutemps.service.EmploiDuTempsService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Emploi du temps", description = "Creneaux horaires et grille hebdomadaire")
public class EmploiDuTempsController {

    private final CreneauHoraireService creneauHoraireService;
    private final EmploiDuTempsService emploiDuTempsService;

    public EmploiDuTempsController(CreneauHoraireService creneauHoraireService, EmploiDuTempsService emploiDuTempsService) {
        this.creneauHoraireService = creneauHoraireService;
        this.emploiDuTempsService = emploiDuTempsService;
    }

    @GetMapping("/api/v1/creneaux-horaires")
    public ResponseEntity<List<CreneauHoraireDto.Profil>> listerCreneaux(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(creneauHoraireService.lister(principal.etablissementId()));
    }

    @PostMapping("/api/v1/creneaux-horaires")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<CreneauHoraireDto.Profil> creerCreneau(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                   @Valid @RequestBody CreneauHoraireDto.Creation requete) {
        return ResponseEntity.ok(creneauHoraireService.creer(principal.etablissementId(), requete));
    }

    @GetMapping("/api/v1/emploi-du-temps/classes/{classeId}")
    @Operation(summary = "Grille hebdomadaire d'une classe pour l'annee active")
    public ResponseEntity<List<EmploiDuTempsDto.Profil>> parClasse(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long classeId) {
        return ResponseEntity.ok(emploiDuTempsService.parClasse(principal.etablissementId(), classeId));
    }

    @GetMapping("/api/v1/emploi-du-temps/enseignants/{enseignantId}")
    @Operation(summary = "Grille hebdomadaire d'un enseignant pour l'annee active")
    public ResponseEntity<List<EmploiDuTempsDto.Profil>> parEnseignant(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long enseignantId) {
        return ResponseEntity.ok(emploiDuTempsService.parEnseignant(principal.etablissementId(), enseignantId));
    }

    @PostMapping("/api/v1/emploi-du-temps")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Ajoute une seance a la grille (anti-conflit prof/salle/classe verifie)")
    public ResponseEntity<EmploiDuTempsDto.Profil> creerSeance(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                 @Valid @RequestBody EmploiDuTempsDto.Creation requete) {
        return ResponseEntity.ok(emploiDuTempsService.creer(principal.etablissementId(), requete));
    }
}
