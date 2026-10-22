package com.educore.modules.transport.controller;

import com.educore.modules.transport.dto.TransportDto;
import com.educore.modules.transport.service.TransportService;
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
@Tag(name = "Transport", description = "Bus, itineraires, arrets, affectation des eleves")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping("/bus")
    public ResponseEntity<List<TransportDto.BusProfil>> listerBus(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(transportService.listerBus(principal.etablissementId()));
    }

    @PostMapping("/bus")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<TransportDto.BusProfil> creerBus(@AuthenticationPrincipal UtilisateurPrincipal principal, @Valid @RequestBody TransportDto.BusCreation requete) {
        return ResponseEntity.ok(transportService.creerBus(principal.etablissementId(), requete));
    }

    @PostMapping("/itineraires")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<TransportDto.ItineraireProfil> creerItineraire(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                            @Valid @RequestBody TransportDto.ItineraireCreation requete) {
        return ResponseEntity.ok(transportService.creerItineraire(principal.etablissementId(), requete));
    }

    @GetMapping("/itineraires/{id}/arrets")
    public ResponseEntity<List<TransportDto.ArretProfil>> arrets(@PathVariable Long id) {
        return ResponseEntity.ok(transportService.arretsDeLItineraire(id));
    }

    @PostMapping("/itineraires/{id}/arrets")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<TransportDto.ArretProfil> ajouterArret(@PathVariable Long id, @Valid @RequestBody TransportDto.ArretCreation requete) {
        return ResponseEntity.ok(transportService.ajouterArret(id, requete));
    }

    @PostMapping("/eleves/{eleveId}/transport")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<TransportDto.AffectationProfil> affecter(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                       @PathVariable Long eleveId, @Valid @RequestBody TransportDto.AffectationEleve requete) {
        return ResponseEntity.ok(transportService.affecterEleve(principal.etablissementId(), eleveId, requete));
    }
}
