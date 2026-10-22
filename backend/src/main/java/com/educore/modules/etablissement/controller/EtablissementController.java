package com.educore.modules.etablissement.controller;

import com.educore.modules.etablissement.dto.EtablissementDto;
import com.educore.modules.etablissement.dto.JourFerieDto;
import com.educore.modules.etablissement.dto.ModuleActifDto;
import com.educore.modules.etablissement.service.EtablissementService;
import com.educore.modules.etablissement.service.JourFerieService;
import com.educore.modules.etablissement.service.ModuleActifService;
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
 * Gestion de l'etablissement (racine du multi-tenant). Seul /inscription est
 * public (bootstrap) : tout le reste opere sur "son propre" etablissement,
 * jamais par id explicite dans l'URL, pour qu'il soit structurellement
 * impossible de cibler l'etablissement d'un autre tenant par erreur.
 */
@RestController
@RequestMapping("/api/v1/etablissements")
@Tag(name = "Etablissements", description = "Inscription, profil et parametrage de l'etablissement")
public class EtablissementController {

    private final EtablissementService etablissementService;
    private final ModuleActifService moduleActifService;
    private final JourFerieService jourFerieService;

    public EtablissementController(EtablissementService etablissementService, ModuleActifService moduleActifService,
                                    JourFerieService jourFerieService) {
        this.etablissementService = etablissementService;
        this.moduleActifService = moduleActifService;
        this.jourFerieService = jourFerieService;
    }

    @PostMapping("/inscription")
    @Operation(summary = "Inscription en libre-service : cree l'etablissement et son premier compte ADMIN")
    public ResponseEntity<EtablissementDto.InscriptionReponse> inscription(@Valid @RequestBody EtablissementDto.Inscription requete) {
        return ResponseEntity.ok(etablissementService.inscrire(requete));
    }

    @GetMapping("/moi")
    @Operation(summary = "Profil de l'etablissement de l'utilisateur connecte")
    public ResponseEntity<EtablissementDto.Profil> moi(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(etablissementService.obtenirProfil(principal.etablissementId()));
    }

    @PutMapping("/moi")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Modifie les informations de l'etablissement")
    public ResponseEntity<EtablissementDto.Profil> modifier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                              @Valid @RequestBody EtablissementDto.Modification requete) {
        return ResponseEntity.ok(etablissementService.modifier(principal.etablissementId(), requete));
    }

    @GetMapping("/moi/modules")
    @Operation(summary = "Liste des modules disponibles et leur etat actif/inactif pour cet etablissement")
    public ResponseEntity<List<ModuleActifDto.Profil>> modules(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(moduleActifService.lister(principal.etablissementId()));
    }

    @PatchMapping("/moi/modules/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Active ou desactive un module pour cet etablissement")
    public ResponseEntity<ModuleActifDto.Profil> definirModule(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                 @PathVariable String code,
                                                                 @Valid @RequestBody ModuleActifDto.Modification requete) {
        return ResponseEntity.ok(moduleActifService.definir(principal.etablissementId(), code, requete.actif()));
    }

    @GetMapping("/moi/jours-feries")
    @Operation(summary = "Liste les jours feries configures pour cet etablissement")
    public ResponseEntity<List<JourFerieDto.Profil>> joursFeries(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(jourFerieService.lister(principal.etablissementId()));
    }

    @PostMapping("/moi/jours-feries")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Ajoute un jour ferie")
    public ResponseEntity<JourFerieDto.Profil> creerJourFerie(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @Valid @RequestBody JourFerieDto.Creation requete) {
        return ResponseEntity.ok(jourFerieService.creer(principal.etablissementId(), requete));
    }

    @DeleteMapping("/moi/jours-feries/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Supprime un jour ferie")
    public ResponseEntity<Void> supprimerJourFerie(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        jourFerieService.supprimer(principal.etablissementId(), id);
        return ResponseEntity.noContent().build();
    }
}
