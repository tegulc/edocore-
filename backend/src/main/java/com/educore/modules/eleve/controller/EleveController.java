package com.educore.modules.eleve.controller;

import com.educore.common.dto.PageDto;
import com.educore.common.dto.PaginationUtils;
import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.eleve.dto.InscriptionDto;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.eleve.service.InscriptionService;
import com.educore.modules.parent.dto.EleveParentDto;
import com.educore.modules.parent.service.EleveParentService;
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
@RequestMapping("/api/v1/eleves")
@Tag(name = "Eleves", description = "Dossiers eleves, inscriptions et liens avec les parents")
public class EleveController {

    private final EleveService eleveService;
    private final InscriptionService inscriptionService;
    private final EleveParentService eleveParentService;

    public EleveController(EleveService eleveService, InscriptionService inscriptionService, EleveParentService eleveParentService) {
        this.eleveService = eleveService;
        this.inscriptionService = inscriptionService;
        this.eleveParentService = eleveParentService;
    }

    @GetMapping
    @Operation(summary = "Liste paginee des eleves de l'etablissement")
    public ResponseEntity<PageDto<EleveDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                             @RequestParam(required = false) Integer page,
                                                             @RequestParam(required = false) Integer taille) {
        var resultat = eleveService.lister(principal.etablissementId(), PaginationUtils.construire(page, taille));
        return ResponseEntity.ok(PageDto.depuis(resultat));
    }

    @GetMapping("/moi")
    @PreAuthorize("hasRole('ELEVE')")
    @Operation(summary = "Dossier de l'eleve connecte (compte utilisateur lie)")
    public ResponseEntity<EleveDto.Profil> monProfil(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(eleveService.obtenirMonProfil(principal.etablissementId(), principal.utilisateurId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EleveDto.Profil> obtenir(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(eleveService.obtenir(principal.etablissementId(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE')")
    @Operation(summary = "Cree un dossier eleve")
    public ResponseEntity<EleveDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                   @Valid @RequestBody EleveDto.Creation requete) {
        return ResponseEntity.ok(eleveService.creer(principal.etablissementId(), requete));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE')")
    public ResponseEntity<EleveDto.Profil> modifier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                      @PathVariable Long id, @Valid @RequestBody EleveDto.Modification requete) {
        return ResponseEntity.ok(eleveService.modifier(principal.etablissementId(), id, requete));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Change le statut (actif, transfere, exclu, diplome, abandonne) - jamais de suppression physique")
    public ResponseEntity<Void> changerStatut(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                               @PathVariable Long id, @Valid @RequestBody EleveDto.ChangementStatut requete) {
        eleveService.changerStatut(principal.etablissementId(), id, requete.statut());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/inscriptions")
    @Operation(summary = "Historique des inscriptions (une par annee scolaire)")
    public ResponseEntity<List<InscriptionDto.Profil>> inscriptions(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.historique(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/inscriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE')")
    @Operation(summary = "Inscrit l'eleve dans une classe pour l'annee scolaire active")
    public ResponseEntity<InscriptionDto.Profil> inscrire(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @PathVariable Long id, @Valid @RequestBody InscriptionDto.Creation requete) {
        return ResponseEntity.ok(inscriptionService.inscrire(principal.etablissementId(), id, requete));
    }

    @GetMapping("/{id}/parents")
    @Operation(summary = "Liste les parents/tuteurs lies a cet eleve")
    public ResponseEntity<List<EleveParentDto.Profil>> parents(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(eleveParentService.listerParents(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/parents")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE')")
    @Operation(summary = "Lie un parent existant a cet eleve")
    public ResponseEntity<EleveParentDto.Profil> lierParent(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                              @PathVariable Long id, @Valid @RequestBody EleveParentDto.Liaison requete) {
        return ResponseEntity.ok(eleveParentService.lier(principal.etablissementId(), id, requete));
    }
}
