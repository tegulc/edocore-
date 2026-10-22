package com.educore.modules.dashboard.controller;

import com.educore.modules.dashboard.dto.DashboardDto;
import com.educore.modules.dashboard.service.DashboardService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Indicateurs consolides de l'etablissement")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/indicateurs")
    public ResponseEntity<DashboardDto.Indicateurs> indicateurs(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(dashboardService.indicateurs(principal.etablissementId()));
    }
}
