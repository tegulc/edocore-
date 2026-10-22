package com.educore.modules.classe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ClasseDto {
    public record Creation(@NotNull Long niveauId, Long serieId, @NotBlank String libelle, Long salleId,
                            Long professeurPrincipalId, @Positive Integer capacite) {}

    public record Modification(Long serieId, String libelle, Long salleId, Long professeurPrincipalId, @Positive Integer capacite) {}

    public record Profil(Long id, Long anneeScolaireId, Long niveauId, String niveauCode, Long serieId, String serieCode,
                          String sousSerieCode, String orientation,
                          String libelle, Long salleId, Long professeurPrincipalId, Integer capacite, long effectif) {}
}
