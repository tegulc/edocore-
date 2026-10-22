package com.educore.modules.classe.dto;

import jakarta.validation.constraints.NotBlank;

public class SerieDto {
    public record Creation(@NotBlank String code, @NotBlank String libelle, String sousSerieCode, 
                          Long serieParentId, Long sousSystemeId, String orientation, String description) {}
    public record Profil(Long id, String code, String sousSerieCode, String libelle, 
                        Long serieParentId, Long sousSystemeId, String orientation, String description) {}
}
