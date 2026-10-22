package com.educore.modules.apipublique.controller;

import com.educore.modules.apipublique.dto.ApiClientDto;
import com.educore.modules.apipublique.service.ApiClientService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestion des clients API partenaires (Module 30). Reservee a ADMIN : donner acces a l'API publique est une decision sensible. */
@RestController
@RequestMapping("/api/v1/api-clients")
@Tag(name = "API publique", description = "Clients OAuth2 pour integrations partenaires (Moodle, Microsoft 365...)")
@PreAuthorize("hasRole('ADMIN')")
public class ApiClientController {

    private final ApiClientService apiClientService;

    public ApiClientController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @GetMapping
    public ResponseEntity<List<ApiClientDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(apiClientService.lister(principal.etablissementId()));
    }

    @PostMapping
    @Operation(summary = "Cree un client API : le client_secret n'est affiche qu'une seule fois")
    public ResponseEntity<ApiClientDto.CreationReponse> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @Valid @RequestBody ApiClientDto.Creation requete) {
        return ResponseEntity.ok(apiClientService.creer(principal.etablissementId(), requete));
    }
}
