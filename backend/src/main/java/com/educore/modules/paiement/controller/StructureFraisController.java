package com.educore.modules.paiement.controller;

import com.educore.modules.etablissement.service.AnneeScolaireService;
import com.educore.modules.paiement.dto.StructureFraisDto;
import com.educore.modules.paiement.service.StructureFraisService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/structures-frais")
@Tag(name = "Structures de frais", description = "Grille tarifaire par niveau/annee")
public class StructureFraisController {

    private final StructureFraisService structureFraisService;
    private final AnneeScolaireService anneeScolaireService;

    public StructureFraisController(StructureFraisService structureFraisService, AnneeScolaireService anneeScolaireService) {
        this.structureFraisService = structureFraisService;
        this.anneeScolaireService = anneeScolaireService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'ECONOME', 'COMPTABLE')")
    public ResponseEntity<StructureFraisDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                            @Valid @RequestBody StructureFraisDto.Creation requete) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(principal.etablissementId()).id();
        return ResponseEntity.ok(structureFraisService.creer(principal.etablissementId(), anneeActiveId, requete));
    }
}
