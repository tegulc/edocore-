package com.educore.modules.classe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SalleDto {
    public record Creation(@NotBlank String nom, @NotNull @Positive Integer capacite, String typeSalle) {}
    public record Profil(Long id, String nom, Integer capacite, String typeSalle) {}
}
