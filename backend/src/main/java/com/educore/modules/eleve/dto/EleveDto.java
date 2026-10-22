package com.educore.modules.eleve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class EleveDto {

    public record Creation(
            @NotBlank String nom, @NotBlank String prenom,
            @NotBlank @Pattern(regexp = "[MF]", message = "Sexe attendu : M ou F") String sexe,
            @NotNull @Past LocalDate dateNaissance, String lieuNaissance, String nationalite,
            String religion, String groupeSanguin, String allergies,
            @NotBlank String matricule,
            Long classeId
    ) {}

    public record Modification(
            String nom, String prenom, String lieuNaissance, String nationalite,
            String religion, String groupeSanguin, String allergies, String photoUrl
    ) {}

    public record Profil(
            Long id, String uuid, String matricule, String nom, String prenom, String sexe,
            LocalDate dateNaissance, String lieuNaissance, String nationalite, String groupeSanguin,
            String allergies, String photoUrl, String qrCode, String statut, LocalDate dateInscription
    ) {}

    public record ChangementStatut(@NotBlank String statut) {} // actif, transfere, exclu, diplome, abandonne
}
