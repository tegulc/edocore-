package com.educore.modules.etablissement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class JourFerieDto {

    public record Creation(@NotBlank String libelle, @NotNull LocalDate dateJour, boolean recurrentAnnuel) {}

    public record Profil(Long id, String libelle, LocalDate dateJour, boolean recurrentAnnuel) {}
}
