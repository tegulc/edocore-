package com.educore.modules.paiement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class StructureFraisDto {
    public record Creation(@NotNull Long niveauId, @NotBlank String libelle, @NotNull @Positive BigDecimal montant, boolean obligatoire) {}
    public record Profil(Long id, Long niveauId, String libelle, BigDecimal montant, boolean obligatoire) {}
}
