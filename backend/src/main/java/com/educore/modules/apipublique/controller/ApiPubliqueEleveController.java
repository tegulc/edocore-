package com.educore.modules.apipublique.controller;

import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.eleve.service.EleveService;
import com.educore.security.ApiClientPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exemple d'endpoint reellement expose a l'API publique (ex: synchronisation
 * de listes eleves depuis Moodle - voir Volume 3 §10.3, integrations externes).
 * Reserve strictement a ROLE_API_CLIENT (jamais accessible avec un jeton
 * utilisateur normal, meme ADMIN) : deux surfaces d'API deliberement
 * separees, la publique restant volontairement minimale.
 */
@RestController
@RequestMapping("/api/v1/public/eleves")
@Tag(name = "API publique", description = "Surface minimale exposee aux partenaires authentifies")
@PreAuthorize("hasRole('API_CLIENT')")
public class ApiPubliqueEleveController {

    private final EleveService eleveService;

    public ApiPubliqueEleveController(EleveService eleveService) {
        this.eleveService = eleveService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultation minimale d'un eleve (nom, statut) par un partenaire authentifie")
    public ResponseEntity<EleveDto.Profil> obtenir(@AuthenticationPrincipal ApiClientPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(eleveService.obtenir(principal.etablissementId(), id));
    }
}
