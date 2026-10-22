package com.educore.notifications.notification.controller;

import com.educore.notifications.notification.dto.NotificationDto;
import com.educore.notifications.notification.service.NotificationService;
import com.educore.notifications.security.TenantContext;
import com.educore.notifications.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Envoi et consultation des notifications (SMS/email/push)")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Mes notifications (utilisateur connecte)")
    public ResponseEntity<List<NotificationDto.Profil>> mesNotifications(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(notificationService.mesNotifications(principal.utilisateurId()));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<NotificationDto.Profil> marquerLue(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marquerLue(id, principal.utilisateurId()));
    }

    @PostMapping("/appareils-mobiles")
    @Operation(summary = "Enregistre un jeton push (application mobile)")
    public ResponseEntity<Void> enregistrerAppareil(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                       @Valid @RequestBody NotificationDto.EnregistrementAppareil requete) {
        notificationService.enregistrerAppareil(principal.utilisateurId(), requete);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('SERVICE_INTERNE')")
    @Operation(summary = "Envoi programmatique (educore-backend uniquement, authentifie par X-Service-Key)")
    public ResponseEntity<NotificationDto.Profil> envoyer(@Valid @RequestBody NotificationDto.Creation requete) {
        // Aucun JWT sur un appel service-a-service : le tenant n'est jamais positionne
        // automatiquement (voir JwtAuthenticationFilter). Sans cette ligne, la RLS
        // (FORCE ROW LEVEL SECURITY) bloquerait silencieusement l'ecriture elle-meme.
        TenantContext.definir(requete.etablissementId());
        try {
            return ResponseEntity.ok(notificationService.envoyer(requete));
        } finally {
            TenantContext.effacer();
        }
    }
}
