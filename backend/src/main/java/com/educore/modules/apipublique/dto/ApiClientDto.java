package com.educore.modules.apipublique.dto;

import jakarta.validation.constraints.NotBlank;

public class ApiClientDto {
    public record Creation(@NotBlank String nom) {}
    public record CreationReponse(Long id, String nom, String clientId, String clientSecret) {} // clientSecret affiche une seule fois
    public record Profil(Long id, String nom, String clientId, String statut) {}

    public record DemandeJeton(@NotBlank String clientId, @NotBlank String clientSecret, @NotBlank String grantType) {}
    public record ReponseJeton(String accessToken, String tokenType, long expiresIn) {}
}
