package com.educore.modules.apipublique.controller;

import com.educore.modules.apipublique.dto.ApiClientDto;
import com.educore.modules.apipublique.service.ApiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public (voir SecurityConfig) : c'est le point d'entree standard OAuth2 client_credentials, aucun JWT prealable n'est possible. */
@RestController
@RequestMapping("/api/v1/oauth")
@Tag(name = "API publique", description = "Emission de jeton pour les integrations partenaires (grant_type=client_credentials)")
public class OAuthTokenController {

    private final ApiClientService apiClientService;

    public OAuthTokenController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @PostMapping("/token")
    @Operation(summary = "Echange client_id/client_secret contre un jeton d'acces valable 1h")
    public ResponseEntity<ApiClientDto.ReponseJeton> token(@Valid @RequestBody ApiClientDto.DemandeJeton requete) {
        return ResponseEntity.ok(apiClientService.authentifier(requete));
    }
}
