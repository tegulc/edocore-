package com.educore.notifications.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationDto {

    /** Cree par un appel service-a-service (educore-backend, cle X-Service-Key) ou par ce service lui-meme. */
    public record Creation(@NotNull Long etablissementId, @NotNull Long destinataireId, @NotBlank String categorie,
                            @NotBlank String titre, String contenu, @NotBlank String canal) {}

    public record Profil(Long id, Long destinataireId, String categorie, String titre, String contenu,
                          String canal, String statutEnvoi, boolean lue) {}

    public record EnregistrementAppareil(@NotBlank String plateforme, @NotBlank String pushToken) {}
}
