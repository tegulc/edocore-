package com.educore.modules.comptabilite.dto;

import jakarta.validation.constraints.NotBlank;

public class CompteComptableDto {
    public record Creation(@NotBlank String code, @NotBlank String libelle, @NotBlank String typeCompte) {}
    public record Profil(Long id, String code, String libelle, String typeCompte) {}
}
