package com.educore.modules.emploidutemps.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class EmploiDuTempsDto {

    public record Creation(
            @NotNull Long classeId, @NotNull Long matiereId, @NotNull Long enseignantId, Long salleId,
            @NotNull Long creneauId, @NotNull @Min(1) @Max(7) Short jourSemaine
    ) {}

    public record Profil(Long id, Long classeId, Long matiereId, String matiereLibelle, Long enseignantId,
                          String enseignantNom, Long salleId, Long creneauId, String creneauLibelle, Short jourSemaine) {}
}
