package com.educore.modules.enseignant.controller;

import com.educore.modules.enseignant.dto.EnseignantDto;
import com.educore.modules.enseignant.service.EnseignantService;
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
@RequestMapping("/api/v1/enseignants")
@Tag(name = "Enseignants", description = "Gestion du personnel enseignant")
public class EnseignantController {

    private final EnseignantService enseignantService;

    public EnseignantController(EnseignantService enseignantService) {
        this.enseignantService = enseignantService;
    }

    @GetMapping
    @Operation(summary = "Liste les enseignants de l'etablissement")
    public ResponseEntity<List<EnseignantDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(enseignantService.lister(principal.etablissementId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnseignantDto.Profil> obtenir(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(enseignantService.obtenir(principal.etablissementId(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    @Operation(summary = "Cree un enseignant (compte utilisateur + profil professionnel)")
    public ResponseEntity<EnseignantDto.CreationReponse> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                 @Valid @RequestBody EnseignantDto.Creation requete) {
        return ResponseEntity.ok(enseignantService.creer(principal.etablissementId(), requete));
    }
}
