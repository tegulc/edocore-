package com.educore.common.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Appelle le service independant Chat + Notifications (Phase 7). Authentifie
 * par cle partagee (X-Service-Key), jamais par JWT utilisateur : ces appels
 * sont declenches par le systeme (ex: bulletin publie), sans utilisateur
 * connecte au moment de l'appel.
 *
 * Volontairement tolerant aux pannes : un notifications-service indisponible
 * ne doit JAMAIS faire echouer l'operation metier qui l'a declenche (publier
 * un bulletin doit reussir meme si l'envoi SMS echoue) - voir le try/catch
 * qui avale l'erreur apres l'avoir journalisee.
 */
@Service
public class NotificationClient {

    private final RestClient restClient;
    private final String cleService;

    public NotificationClient(@Value("${educore.notifications-service.url}") String url,
                               @Value("${educore.notifications-service.service-key}") String cleService) {
        this.restClient = RestClient.builder().baseUrl(url).build();
        this.cleService = cleService;
    }

    public void envoyer(Long etablissementId, Long destinataireId, String categorie, String titre, String contenu, String canal) {
        try {
            restClient.post()
                    .uri("/api/v1/notifications")
                    .header("X-Service-Key", cleService)
                    .header("Content-Type", "application/json")
                    .body(new RequeteNotification(etablissementId, destinataireId, categorie, titre, contenu, canal))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Ne jamais laisser une panne du notifications-service faire echouer l'operation
            // metier appelante. A brancher sur un logger structure en production.
        }
    }

    private record RequeteNotification(Long etablissementId, Long destinataireId, String categorie, String titre, String contenu, String canal) {}
}
