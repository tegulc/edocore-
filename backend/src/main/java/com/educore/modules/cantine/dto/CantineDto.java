package com.educore.modules.cantine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CantineDto {

    public record MenuCreation(@NotNull LocalDate dateJour, @NotBlank String libellePlat, String informationsNutritionnelles) {}
    public record MenuProfil(Long id, LocalDate dateJour, String libellePlat, String informationsNutritionnelles) {}

    public record AbonnementCreation(@NotNull Long periodeId, @NotNull @Positive BigDecimal montant) {}
    public record AbonnementProfil(Long id, Long eleveId, Long periodeId, BigDecimal montant, String statutPaiement) {}

    public record PresenceCreation(@NotNull Long eleveId, @NotNull LocalDate dateJour, @NotBlank String statut) {}
    public record PresenceProfil(Long id, Long eleveId, LocalDate dateJour, String statut) {}
}
