package com.educore.modules.enseignant.dto;

import com.educore.modules.auth.dto.UtilisateurDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EnseignantDto {

    public record Creation(
            @NotBlank String nom, @NotBlank String prenom, @NotBlank @Email String email, String telephone,
            String matricule, String specialite, String diplomePrincipal, LocalDate dateEmbauche,
            String typeContrat, BigDecimal salaireBase, BigDecimal tauxHoraireSupp
    ) {}

    public record CreationReponse(Profil enseignant, String motDePasseTemporaire) {}

    public record Profil(
            Long id, UtilisateurDto.Profil utilisateur, String matricule, String specialite,
            String diplomePrincipal, LocalDate dateEmbauche, String typeContrat,
            BigDecimal salaireBase, BigDecimal tauxHoraireSupp, String statut
    ) {}
}
