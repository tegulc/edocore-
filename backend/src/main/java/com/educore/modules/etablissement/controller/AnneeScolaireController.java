package com.educore.modules.etablissement.controller;

import com.educore.modules.etablissement.dto.AnneeScolaireDto;
import com.educore.modules.etablissement.dto.PeriodeDto;
import com.educore.modules.etablissement.dto.VacanceScolaireDto;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import com.educore.modules.etablissement.service.PeriodeService;
import com.educore.modules.etablissement.service.VacanceScolaireService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Annees scolaires, periodes pedagogiques et vacances. Toutes les dependances
 * (periodes, vacances) sont exposees sous /annees-scolaires/{id}/... plutot
 * qu'a plat, pour que l'appartenance a une annee soit explicite dans l'URL
 * et jamais devinee.
 */
@RestController
@RequestMapping("/api/v1/annees-scolaires")
@Tag(name = "Annees scolaires", description = "Annees scolaires, periodes et vacances")
public class AnneeScolaireController {

    private final AnneeScolaireService anneeScolaireService;
    private final PeriodeService periodeService;
    private final VacanceScolaireService vacanceScolaireService;

    public AnneeScolaireController(AnneeScolaireService anneeScolaireService, PeriodeService periodeService,
                                    VacanceScolaireService vacanceScolaireService) {
        this.anneeScolaireService = anneeScolaireService;
        this.periodeService = periodeService;
        this.vacanceScolaireService = vacanceScolaireService;
    }

    @GetMapping
    @Operation(summary = "Liste toutes les annees scolaires de l'etablissement, la plus recente en premier")
    public ResponseEntity<List<AnneeScolaireDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(anneeScolaireService.lister(principal.etablissementId()));
    }

    @GetMapping("/active")
    @Operation(summary = "Annee scolaire actuellement active")
    public ResponseEntity<AnneeScolaireDto.Profil> active(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(anneeScolaireService.obtenirActive(principal.etablissementId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Cree une nouvelle annee scolaire")
    public ResponseEntity<AnneeScolaireDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                           @Valid @RequestBody AnneeScolaireDto.Creation requete) {
        return ResponseEntity.ok(anneeScolaireService.creer(principal.etablissementId(), requete));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Active cette annee (desactive automatiquement toute autre annee active)")
    public ResponseEntity<AnneeScolaireDto.Profil> activer(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(anneeScolaireService.activer(principal.etablissementId(), id));
    }

    @GetMapping("/{id}/periodes")
    @Operation(summary = "Liste les periodes (trimestres/sequences/semestres) de cette annee")
    public ResponseEntity<List<PeriodeDto.Profil>> periodes(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(periodeService.lister(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/periodes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Cree une periode pour cette annee")
    public ResponseEntity<PeriodeDto.Profil> creerPeriode(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @PathVariable Long id, @Valid @RequestBody PeriodeDto.Creation requete) {
        return ResponseEntity.ok(periodeService.creer(principal.etablissementId(), id, requete));
    }

    @PatchMapping("/{id}/periodes/{periodeId}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Cloture une periode (fige les bulletins, irreversible)")
    public ResponseEntity<PeriodeDto.Profil> cloturerPeriode(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                               @PathVariable Long id, @PathVariable Long periodeId) {
        return ResponseEntity.ok(periodeService.cloturer(principal.etablissementId(), id, periodeId));
    }

    @GetMapping("/{id}/vacances")
    @Operation(summary = "Liste les periodes de vacances de cette annee")
    public ResponseEntity<List<VacanceScolaireDto.Profil>> vacances(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(vacanceScolaireService.lister(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/vacances")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Ajoute une periode de vacances")
    public ResponseEntity<VacanceScolaireDto.Profil> creerVacances(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                     @PathVariable Long id, @Valid @RequestBody VacanceScolaireDto.Creation requete) {
        return ResponseEntity.ok(vacanceScolaireService.creer(principal.etablissementId(), id, requete));
    }
}
