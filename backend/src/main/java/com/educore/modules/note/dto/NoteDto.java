package com.educore.modules.note.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class NoteDto {

    public record Ligne(@NotNull Long eleveId, BigDecimal valeur, boolean absent, String observation) {}

    public record SaisieMasse(@NotEmpty @Valid List<Ligne> notes) {}

    public record Profil(Long id, Long evaluationId, Long eleveId, String eleveNom, BigDecimal valeur,
                          boolean absent, String observation, boolean valide) {}
}
