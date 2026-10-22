package com.educore.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class GestionUtilisateurDto {

    public record Creation(
            @NotBlank String nom,
            @NotBlank String prenom,
            @NotBlank @Email String email,
            String telephone,
            /** Premier code = role principal. Au moins un role est obligatoire (cahier des charges Module 1). */
            @NotEmpty List<String> roleCodes,
            String matricule
    ) {}

    public record Modification(
            @NotBlank String nom,
            @NotBlank String prenom,
            String telephone,
            /** Remplace l'integralite des roles attribues ; le premier code devient le role principal. */
            @NotEmpty List<String> roleCodes
    ) {}

    /** Retourne le mot de passe temporaire genere, affiche une seule fois a l'administrateur. */
    public record CreationReponse(UtilisateurDto.Profil utilisateur, String motDePasseTemporaire) {}

    public record ChangementStatut(@NotBlank String statut) {} // ACTIF, INACTIF, SUSPENDU, ARCHIVE

    public record GestionRole(@NotBlank String roleCode) {}
}
