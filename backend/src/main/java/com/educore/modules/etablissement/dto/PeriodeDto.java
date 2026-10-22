package com.educore.modules.etablissement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class PeriodeDto {

    public record Creation(
            @NotBlank String typePeriode, // trimestre, sequence, semestre
            @NotNull @Positive Short numero,
            @NotBlank String libelle,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin
    ) {}

    public record Profil(
            Long id,
            Long anneeScolaireId,
            String typePeriode,
            Short numero,
            String libelle,
            LocalDate dateDebut,
            LocalDate dateFin,
            boolean estCloturee
    ) {}
}
