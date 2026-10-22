package com.educore.modules.classe.dto;

import jakarta.validation.constraints.NotBlank;

public class MatiereDto {
    public record Creation(@NotBlank String code, @NotBlank String libelle, String couleur, 
                          String typeMatiere, Short langueVivanteNiveau, Boolean isOption, String description) {}
    public record Profil(Long id, String code, String libelle, String couleur, 
                        String typeMatiere, Short langueVivanteNiveau, Boolean isOption, String description) {}
}
