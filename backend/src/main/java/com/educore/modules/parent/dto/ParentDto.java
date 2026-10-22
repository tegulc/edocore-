package com.educore.modules.parent.dto;

import com.educore.modules.auth.dto.UtilisateurDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ParentDto {

    public record Creation(
            @NotBlank String nom, @NotBlank String prenom, @NotBlank @Email String email,
            String telephone, String profession, String adresse
    ) {}

    public record CreationReponse(Profil parent, String motDePasseTemporaire) {}

    public record Profil(Long id, UtilisateurDto.Profil utilisateur, String profession, String adresse) {}
}
