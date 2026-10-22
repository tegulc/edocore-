package com.educore.modules.personnel.controller;

import com.educore.modules.personnel.dto.PersonnelDto;
import com.educore.modules.personnel.service.PersonnelService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personnel")
@Tag(name = "Personnel", description = "Module 20 — Gestion du personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<List<PersonnelDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(personnelService.lister(principal.etablissementId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<PersonnelDto.CreationReponse> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                               @Valid @RequestBody PersonnelDto.Creation requete) {
        return ResponseEntity.ok(personnelService.creer(principal.etablissementId(), requete));
    }

    @PostMapping("/conges")
    public ResponseEntity<PersonnelDto.DemandeCongeProfil> demanderConge(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                          @Valid @RequestBody PersonnelDto.DemandeCongeCreation requete) {
        return ResponseEntity.ok(personnelService.demanderConge(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @GetMapping("/conges/moi")
    public ResponseEntity<List<PersonnelDto.DemandeCongeProfil>> mesConges(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(personnelService.mesConges(principal.etablissementId(), principal.utilisateurId()));
    }

    @GetMapping("/conges/en-attente")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<List<PersonnelDto.DemandeCongeProfil>> congesEnAttente(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(personnelService.congesEnAttente(principal.etablissementId()));
    }

    @PatchMapping("/conges/{congeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<PersonnelDto.DemandeCongeProfil> validerConge(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                         @PathVariable Long congeId,
                                                                         @Valid @RequestBody PersonnelDto.ValidationConge requete) {
        return ResponseEntity.ok(personnelService.validerConge(principal.etablissementId(), principal.utilisateurId(), congeId, requete));
    }
}
