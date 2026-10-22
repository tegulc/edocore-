package com.educore.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class DocumentDto {

    private DocumentDto() {}

    public record Creation(
            @NotBlank String proprietaireType,
            @NotNull Long proprietaireId,
            @NotBlank String typeDocument,
            @NotBlank String titre,
            @NotBlank String fichierUrl
    ) {}

    public record Profil(
            Long id,
            String proprietaireType,
            Long proprietaireId,
            String typeDocument,
            String titre,
            String fichierUrl,
            Boolean archive,
            Instant dateAjout
    ) {}
}
