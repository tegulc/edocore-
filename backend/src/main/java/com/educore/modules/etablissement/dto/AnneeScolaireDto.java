package com.educore.modules.etablissement.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AnneeScolaireDto {

    public record Creation(
            @NotBlank String libelle,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin
    ) {}

    public record Profil(
            Long id,
            String libelle,
            LocalDate dateDebut,
            LocalDate dateFin,
            boolean estActive
    ) {}
}
