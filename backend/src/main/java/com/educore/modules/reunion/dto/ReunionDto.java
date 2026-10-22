package com.educore.modules.reunion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class ReunionDto {

    public record Creation(@NotBlank String titre, String description, @NotNull Instant dateReunion,
                            String lieu, String lienVisioconference, @NotEmpty List<Long> invitesIds) {}

    public record Profil(Long id, String titre, String description, Instant dateReunion, String lieu,
                          String lienVisioconference, Long organisateurId) {}

    public record Reponse(@NotBlank String statutReponse) {} // accepte, decline

    public record InviteProfil(Long utilisateurId, String statutReponse, Boolean presentEffectif) {}

    public record CompteRenduCreation(@NotBlank String contenu, String fichierUrl) {}
    public record CompteRenduProfil(Long id, Long reunionId, String contenu, String fichierUrl) {}
}
