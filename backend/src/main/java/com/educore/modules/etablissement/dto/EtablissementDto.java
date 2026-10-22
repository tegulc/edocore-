package com.educore.modules.etablissement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class EtablissementDto {

    /**
     * Inscription en libre-service (bootstrap) : cree l'etablissement ET son
     * premier compte ADMIN dans la meme transaction. Endpoint public
     * (POST /etablissements/inscription) - le seul de tout le module a ne
     * pas exiger d'authentification, puisqu'aucun compte n'existe encore.
     */
    public record Inscription(
            @NotBlank String nom,
            String sigle,
            @NotBlank String pays,
            String ville,
            String typeEtablissement,
            String telephone,
            @Email String email,
            @Valid PremierAdmin administrateur
    ) {}

    public record PremierAdmin(
            @NotBlank String nom,
            @NotBlank String prenom,
            @NotBlank @Email String email,
            String telephone
    ) {}

    public record InscriptionReponse(
            Profil etablissement,
            com.educore.modules.auth.dto.UtilisateurDto.Profil administrateur,
            String motDePasseTemporaire
    ) {}

    public record Profil(
            Long id,
            String nom,
            String sigle,
            String logoUrl,
            String pays,
            String ville,
            String adresse,
            String telephone,
            String email,
            String typeEtablissement,
            String statut,
            String planAbonnement,
            String devise,
            String langueDefaut,
            String fuseauHoraire
    ) {}

    public record Modification(
            @NotBlank @Size(max = 200) String nom,
            String sigle,
            String logoUrl,
            String ville,
            String adresse,
            String telephone,
            @Email String email,
            String typeEtablissement,
            String devise,
            String langueDefaut,
            String fuseauHoraire
    ) {}
}
