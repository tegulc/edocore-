package com.educore.modules.paiement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EcheancierDto {
    public record Generation(@NotNull Long structureFraisId, @Min(1) @Max(12) int nombreTranches, @NotNull LocalDate premiereEcheance) {}

    public record Profil(Long id, Long eleveId, Long structureFraisId, BigDecimal montantTotal, int nombreTranches, List<TrancheDto.Profil> tranches) {}
}
