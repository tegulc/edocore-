package com.educore.modules.classe.controller;

import com.educore.modules.classe.dto.MatiereDto;
import com.educore.modules.classe.dto.NiveauDto;
import com.educore.modules.classe.dto.SalleDto;
import com.educore.modules.classe.dto.SerieDto;
import com.educore.modules.classe.service.MatiereService;
import com.educore.modules.classe.service.NiveauService;
import com.educore.modules.classe.service.SalleService;
import com.educore.modules.classe.service.SerieService;
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
 * Referentiels de la structure pedagogique (Module 5) : niveaux, series, salles,
 * matieres. Donnees de configuration simples, en lecture pour tous les roles
 * connectes, en ecriture reservees a ADMIN/DIRECTEUR.
 */
@RestController
@Tag(name = "Structure pedagogique", description = "Niveaux, series, salles, matieres")
public class StructurePedagogiqueController {

    private final NiveauService niveauService;
    private final SerieService serieService;
    private final SalleService salleService;
    private final MatiereService matiereService;

    public StructurePedagogiqueController(NiveauService niveauService, SerieService serieService,
                                           SalleService salleService, MatiereService matiereService) {
        this.niveauService = niveauService;
        this.serieService = serieService;
        this.salleService = salleService;
        this.matiereService = matiereService;
    }

    @GetMapping("/api/v1/niveaux")
    public ResponseEntity<List<NiveauDto.Profil>> listerNiveaux(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(niveauService.lister(principal.etablissementId()));
    }

    @PostMapping("/api/v1/niveaux")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Cree un niveau (6e, 5e, Tle...)")
    public ResponseEntity<NiveauDto.Profil> creerNiveau(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                          @Valid @RequestBody NiveauDto.Creation requete) {
        return ResponseEntity.ok(niveauService.creer(principal.etablissementId(), requete));
    }

    @GetMapping("/api/v1/series")
    public ResponseEntity<List<SerieDto.Profil>> listerSeries(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(serieService.lister(principal.etablissementId()));
    }

    @PostMapping("/api/v1/series")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<SerieDto.Profil> creerSerie(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                        @Valid @RequestBody SerieDto.Creation requete) {
        return ResponseEntity.ok(serieService.creer(principal.etablissementId(), requete));
    }

    @GetMapping("/api/v1/salles")
    public ResponseEntity<List<SalleDto.Profil>> listerSalles(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(salleService.lister(principal.etablissementId()));
    }

    @PostMapping("/api/v1/salles")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<SalleDto.Profil> creerSalle(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                        @Valid @RequestBody SalleDto.Creation requete) {
        return ResponseEntity.ok(salleService.creer(principal.etablissementId(), requete));
    }

    @GetMapping("/api/v1/matieres")
    public ResponseEntity<List<MatiereDto.Profil>> listerMatieres(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(matiereService.lister(principal.etablissementId()));
    }

    @PostMapping("/api/v1/matieres")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<MatiereDto.Profil> creerMatiere(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @Valid @RequestBody MatiereDto.Creation requete) {
        return ResponseEntity.ok(matiereService.creer(principal.etablissementId(), requete));
    }
}
