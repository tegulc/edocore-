package com.educore.modules.infirmerie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class InfirmerieDto {

    public record ConsultationCreation(@NotNull Long eleveId, @NotBlank String motif, String diagnostic, String traitement) {}
    public record ConsultationProfil(Long id, Long eleveId, String dateConsultation, String motif, String diagnostic, String traitement) {}

    public record UrgenceCreation(@NotNull Long eleveId, @NotBlank String description, String actionPrise, String hopitalTransfert) {}
    public record UrgenceProfil(Long id, Long eleveId, String dateUrgence, String description, String actionPrise, String hopitalTransfert, boolean parentNotifie) {}

    public record VaccinCreation(@NotNull Long eleveId, @NotBlank String nomVaccin, @NotNull LocalDate dateAdministration, LocalDate rappelPrevu) {}
    public record VaccinProfil(Long id, Long eleveId, String nomVaccin, LocalDate dateAdministration, LocalDate rappelPrevu) {}
}
