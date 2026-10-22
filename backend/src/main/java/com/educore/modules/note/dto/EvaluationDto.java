package com.educore.modules.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationDto {
    public record Creation(@NotNull Long classeMatiereId, @NotBlank String typeEvaluationCode, @NotNull Long periodeId,
                            @NotBlank String titre, @NotNull LocalDate dateEvaluation, BigDecimal bareme) {}

    public record Profil(Long id, Long classeMatiereId, String typeEvaluationCode, Long periodeId, String titre,
                          LocalDate dateEvaluation, BigDecimal bareme, String statut) {}
}
