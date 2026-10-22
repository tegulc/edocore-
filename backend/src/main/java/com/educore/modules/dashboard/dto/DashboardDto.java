package com.educore.modules.dashboard.dto;

import java.math.BigDecimal;

public class DashboardDto {
    /** Correspond exactement aux 4 cartes indicateurs posees en Phase 0 (PageTableauDeBord.tsx). */
    public record Indicateurs(long elevesInscrits, Double tauxPresenceDuJour, BigDecimal paiementsDuMois, long personnelActif) {}
}
