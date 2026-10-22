package com.educore.modules.classe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NiveauDto {
    public record Creation(@NotBlank String code, @NotBlank String libelle, @NotNull Short ordre, String cycle) {}
    public record Profil(Long id, String code, String libelle, Short ordre, String cycle) {}
}
