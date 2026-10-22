package com.educore.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangerMotDePasseDto {
    public record Requete(
            @NotBlank String ancienMotDePasse,
            @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres") String nouveauMotDePasse
    ) {}
}
