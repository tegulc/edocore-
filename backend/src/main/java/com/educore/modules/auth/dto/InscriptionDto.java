package com.educore.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InscriptionDto {

    public record Requete(
            @NotBlank String nom,
            @NotBlank String prenom,
            @NotBlank @Email String email,
            @NotBlank String motDePasse,
            @NotNull Long etablissementId
    ) {}

}
