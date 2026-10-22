package com.educore.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class DeuxFaDto {

    /** Retourne au moment de l'activation : le secret + l'URI a encoder en QR code (affiches une seule fois). */
    public record Provisionnement(String secretBase32, String uriProvisionnement) {}

    public record ConfirmerActivation(@NotBlank String code) {}

    /**
     * Etape 2 de la connexion quand le compte a le 2FA actif (POST /auth/2fa/verifier,
     * conforme au Volume 6, Module 1). Distinct de ConfirmerActivation : celui-ci clot
     * une connexion en cours, celui-la active le 2FA sur le compte pour la premiere fois.
     */
    public record Verification(@NotBlank String jetonDeuxFa, @NotBlank String code) {}
}
