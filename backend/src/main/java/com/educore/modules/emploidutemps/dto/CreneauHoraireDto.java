package com.educore.modules.emploidutemps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class CreneauHoraireDto {
    public record Creation(@NotBlank String libelle, @NotNull LocalTime heureDebut, @NotNull LocalTime heureFin, @NotNull Short ordre) {}
    public record Profil(Long id, String libelle, LocalTime heureDebut, LocalTime heureFin, Short ordre) {}
}
