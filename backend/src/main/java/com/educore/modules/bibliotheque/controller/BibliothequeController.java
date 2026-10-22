package com.educore.modules.bibliotheque.controller;

import com.educore.modules.bibliotheque.dto.EmpruntDto;
import com.educore.modules.bibliotheque.dto.LivreDto;
import com.educore.modules.bibliotheque.service.BibliothequeService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Bibliotheque", description = "Catalogue et emprunts")
public class BibliothequeController {

    private final BibliothequeService bibliothequeService;

    public BibliothequeController(BibliothequeService bibliothequeService) {
        this.bibliothequeService = bibliothequeService;
    }

    @GetMapping("/livres")
    public ResponseEntity<List<LivreDto.Profil>> catalogue(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(bibliothequeService.catalogue(principal.etablissementId()));
    }

    @PostMapping("/livres")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'BIBLIOTHECAIRE')")
    public ResponseEntity<LivreDto.Profil> ajouter(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody LivreDto.Creation requete) {
        return ResponseEntity.ok(bibliothequeService.ajouter(principal.etablissementId(), requete));
    }

    @PostMapping("/emprunts")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'BIBLIOTHECAIRE')")
    public ResponseEntity<EmpruntDto.Profil> emprunter(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody EmpruntDto.Creation requete) {
        return ResponseEntity.ok(bibliothequeService.emprunter(principal.etablissementId(), requete));
    }

    @PatchMapping("/emprunts/{id}/retourner")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'BIBLIOTHECAIRE')")
    public ResponseEntity<EmpruntDto.Profil> retourner(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(bibliothequeService.retourner(principal.etablissementId(), id));
    }

    @GetMapping("/utilisateurs/{utilisateurId}/emprunts")
    public ResponseEntity<List<EmpruntDto.Profil>> historique(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(bibliothequeService.historiquePourUtilisateur(utilisateurId));
    }
}
