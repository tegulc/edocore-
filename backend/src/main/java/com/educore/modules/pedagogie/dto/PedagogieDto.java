package com.educore.modules.pedagogie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class PedagogieDto {

    private PedagogieDto() {}

    public record CahierCreation(
            @NotNull Long classeMatiereId,
            @NotNull LocalDate dateCours,
            String chapitre,
            String lecon,
            String contenu,
            String exercices
    ) {}

    public record CahierProfil(
            Long id,
            Long classeMatiereId,
            LocalDate dateCours,
            String chapitre,
            String lecon,
            String contenu,
            String exercices,
            Instant createdAt
    ) {}

    public record DevoirCreation(
            @NotNull Long classeMatiereId,
            @NotBlank String titre,
            String description,
            @NotNull LocalDate dateLimite,
            String fichierSujetUrl
    ) {}

    public record DevoirProfil(
            Long id,
            Long classeMatiereId,
            String titre,
            String description,
            LocalDate datePublication,
            LocalDate dateLimite,
            String fichierSujetUrl,
            Instant createdAt
    ) {}

    public record DevoirRenduCreation(
            @NotNull Long devoirId,
            String fichierReponseUrl
    ) {}

    public record DevoirCorrection(
            BigDecimal note,
            String correctionCommentaire
    ) {}

    public record DevoirRenduProfil(
            Long id,
            Long devoirId,
            Long eleveId,
            String fichierReponseUrl,
            Instant dateSoumission,
            BigDecimal note,
            String correctionCommentaire,
            String statut
    ) {}
}
