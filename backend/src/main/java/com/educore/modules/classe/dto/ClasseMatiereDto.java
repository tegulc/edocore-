package com.educore.modules.classe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ClasseMatiereDto {
    public record Affectation(@NotNull Long matiereId, Long enseignantId,
                               @NotNull @DecimalMin("0.1") BigDecimal coefficient,
                               @NotNull @DecimalMin("0.1") BigDecimal volumeHoraireHebdo) {}

    public record Profil(Long id, Long classeId, Long matiereId, String matiereLibelle, Long enseignantId,
                          BigDecimal coefficient, BigDecimal volumeHoraireHebdo) {}
}
