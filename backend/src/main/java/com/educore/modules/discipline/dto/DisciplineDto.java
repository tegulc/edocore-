package com.educore.modules.discipline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class DisciplineDto {

    public record IncidentCreation(@NotNull Long eleveId, @NotNull LocalDate dateIncident, @NotBlank String description) {}
    public record IncidentProfil(Long id, Long eleveId, LocalDate dateIncident, String description) {}

    public record SanctionCreation(@NotNull Long eleveId, Long incidentId, @NotBlank String typeSanctionCode, Integer dureeJours, String motif) {}
    public record SanctionProfil(Long id, Long eleveId, Long incidentId, String typeSanctionCode, String typeSanctionLibelle,
                                  LocalDate dateSanction, Integer dureeJours, String motif) {}
}
