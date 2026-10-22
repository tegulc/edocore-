package com.educore.modules.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PresenceDto {

    public record Enregistrement(
            @NotNull Long eleveId, Long classeMatiereId, @NotNull LocalDate dateJour,
            @NotBlank String typePresence, String methodePointage
    ) {}

    public record Justification(@NotBlank String motif) {}

    public record Profil(Long id, Long eleveId, Long classeMatiereId, LocalDate dateJour,
                          String typePresence, boolean justifie, String motifJustification) {}

    public record Bilan(long absencesNonJustifiees, long retards) {}
}
