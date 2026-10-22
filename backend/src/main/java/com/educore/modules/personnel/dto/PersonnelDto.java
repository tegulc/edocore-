package com.educore.modules.personnel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class PersonnelDto {

    private PersonnelDto() {}

    public record Creation(
            @NotBlank String nom,
            @NotBlank String prenom,
            @NotBlank String email,
            String telephone,
            String matricule,
            @NotBlank String roleCode,
            String fonction,
            String departement,
            LocalDate dateEmbauche,
            String typeContrat,
            BigDecimal salaireBase
    ) {}

    public record CreationReponse(Profil profil, String motDePasseTemporaire) {}

    public record Profil(
            Long id,
            Long utilisateurId,
            String matricule,
            String nom,
            String prenom,
            String email,
            String fonction,
            String departement,
            LocalDate dateEmbauche,
            String typeContrat,
            BigDecimal salaireBase,
            String statut
    ) {}

    public record DemandeCongeCreation(
            @NotBlank String typeConge,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin,
            String motif
    ) {}

    public record DemandeCongeProfil(
            Long id,
            Long utilisateurId,
            String typeConge,
            LocalDate dateDebut,
            LocalDate dateFin,
            String motif,
            String statut,
            Instant createdAt
    ) {}

    public record ValidationConge(@NotBlank String statut) {}
}
