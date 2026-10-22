package com.educore.modules.auth.controller;

import com.educore.common.dto.PageDto;
import com.educore.common.dto.PaginationUtils;
import com.educore.common.util.RequeteUtils;
import com.educore.modules.auth.dto.GestionUtilisateurDto;
import com.educore.modules.auth.dto.UtilisateurDto;
import com.educore.modules.auth.service.UtilisateurAdminService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Gestion des comptes utilisateurs d'un etablissement. Reserve aux
 * administrateurs de l'etablissement (ADMIN, DIRECTEUR) — controle macro
 * ici (fonctionne desormais correctement avec le multi-role puisque
 * JwtAuthenticationFilter cree une GrantedAuthority par role attribue).
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@Tag(name = "Utilisateurs", description = "Gestion administrative des comptes (creation, modification, statut, roles)")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
public class UtilisateurAdminController {

    private final UtilisateurAdminService utilisateurAdminService;

    public UtilisateurAdminController(UtilisateurAdminService utilisateurAdminService) {
        this.utilisateurAdminService = utilisateurAdminService;
    }

    @GetMapping
    @Operation(summary = "Liste paginee des utilisateurs de l'etablissement courant")
    public ResponseEntity<PageDto<UtilisateurDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer taille) {
        var resultat = utilisateurAdminService.lister(principal.etablissementId(), PaginationUtils.construire(page, taille));
        return ResponseEntity.ok(PageDto.depuis(resultat));
    }

    @PostMapping
    @Operation(summary = "Cree un utilisateur (un ou plusieurs roles) avec un mot de passe temporaire")
    public ResponseEntity<GestionUtilisateurDto.CreationReponse> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                        HttpServletRequest requeteHttp,
                                                                        @Valid @RequestBody GestionUtilisateurDto.Creation requete) {
        var reponse = utilisateurAdminService.creer(principal.etablissementId(), principal.utilisateurId(),
                RequeteUtils.extraireIp(requeteHttp), requete);
        return ResponseEntity.ok(reponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifie un utilisateur existant (remplace l'ensemble de ses roles)")
    public ResponseEntity<UtilisateurDto.Profil> modifier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            HttpServletRequest requeteHttp,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody GestionUtilisateurDto.Modification requete) {
        var reponse = utilisateurAdminService.modifier(principal.etablissementId(), principal.utilisateurId(),
                RequeteUtils.extraireIp(requeteHttp), id, requete);
        return ResponseEntity.ok(reponse);
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Change le statut d'un utilisateur (actif, suspendu, archive...)")
    public ResponseEntity<Void> changerStatut(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                               HttpServletRequest requeteHttp,
                                               @PathVariable Long id,
                                               @Valid @RequestBody GestionUtilisateurDto.ChangementStatut requete) {
        utilisateurAdminService.changerStatut(principal.etablissementId(), principal.utilisateurId(),
                RequeteUtils.extraireIp(requeteHttp), id, requete.statut());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Ajoute un role supplementaire a un utilisateur (multi-role)")
    public ResponseEntity<Void> ajouterRole(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                             HttpServletRequest requeteHttp,
                                             @PathVariable Long id,
                                             @Valid @RequestBody GestionUtilisateurDto.GestionRole requete) {
        utilisateurAdminService.ajouterRole(principal.etablissementId(), principal.utilisateurId(),
                RequeteUtils.extraireIp(requeteHttp), id, requete.roleCode());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/roles/{roleCode}")
    @Operation(summary = "Retire un role a un utilisateur (refuse si c'est son dernier role)")
    public ResponseEntity<Void> retirerRole(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                             HttpServletRequest requeteHttp,
                                             @PathVariable Long id,
                                             @PathVariable String roleCode) {
        utilisateurAdminService.retirerRole(principal.etablissementId(), principal.utilisateurId(),
                RequeteUtils.extraireIp(requeteHttp), id, roleCode);
        return ResponseEntity.noContent().build();
    }
}
