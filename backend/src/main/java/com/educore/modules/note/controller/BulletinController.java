package com.educore.modules.note.controller;

import com.educore.modules.note.dto.BulletinDto;
import com.educore.modules.note.dto.MoyenneDto;
import com.educore.modules.note.service.BulletinService;
import com.educore.modules.note.service.MoyenneCalculService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Moyennes & Bulletins", description = "Calcul des moyennes et generation des bulletins")
public class BulletinController {

    private final MoyenneCalculService moyenneCalculService;
    private final BulletinService bulletinService;

    public BulletinController(MoyenneCalculService moyenneCalculService, BulletinService bulletinService) {
        this.moyenneCalculService = moyenneCalculService;
        this.bulletinService = bulletinService;
    }

    @PostMapping("/api/v1/classes/{classeId}/moyennes/recalculer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Recalcule les moyennes matiere/generale + classement de toute la classe pour une periode (a partir des evaluations validees)")
    public ResponseEntity<List<MoyenneDto.Generale>> recalculerMoyennes(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                          @PathVariable Long classeId, @RequestParam Long periodeId) {
        return ResponseEntity.ok(moyenneCalculService.recalculerPourClasse(principal.etablissementId(), classeId, periodeId));
    }

    @PostMapping("/api/v1/classes/{classeId}/bulletins/generer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Genere (ou regenere) les bulletins de toute la classe pour une periode, a partir des moyennes deja calculees")
    public ResponseEntity<List<BulletinDto.Profil>> genererBulletins(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                        @PathVariable Long classeId, @RequestParam Long periodeId) {
        return ResponseEntity.ok(bulletinService.genererPourClasse(principal.etablissementId(), classeId, periodeId));
    }

    @PatchMapping("/api/v1/bulletins/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<BulletinDto.Profil> validerBulletin(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(bulletinService.valider(principal.etablissementId(), id));
    }

    @PatchMapping("/api/v1/bulletins/{id}/publier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Publie le bulletin (visible parents/eleves)")
    public ResponseEntity<BulletinDto.Profil> publierBulletin(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(bulletinService.publier(principal.etablissementId(), id));
    }

    @GetMapping("/api/v1/eleves/{eleveId}/bulletins/{periodeId}")
    @Operation(summary = "Consulte le bulletin d'un eleve pour une periode")
    public ResponseEntity<BulletinDto.Profil> obtenirBulletin(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                 @PathVariable Long eleveId, @PathVariable Long periodeId) {
        return ResponseEntity.ok(bulletinService.obtenirPourEleve(principal.etablissementId(), eleveId, periodeId));
    }

    @PostMapping("/api/v1/bulletins/{id}/conseil")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Enregistre les decisions du conseil de classe")
    public ResponseEntity<BulletinDto.Profil> enregistrerConseilClasse(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                         @PathVariable Long id,
                                                                         @RequestBody BulletinDto.ConseilClasse conseil) {
        return ResponseEntity.ok(bulletinService.enregistrerConseilClasse(principal.etablissementId(), id, conseil));
    }
}
