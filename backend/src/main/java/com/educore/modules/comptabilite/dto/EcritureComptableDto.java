package com.educore.modules.comptabilite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EcritureComptableDto {
    public record Creation(@NotNull Long compteId, @NotNull LocalDate dateEcriture, @NotBlank String libelle,
                            BigDecimal debit, BigDecimal credit, String pieceJustificativeUrl) {}

    public record Profil(Long id, Long compteId, String compteCode, LocalDate dateEcriture, String libelle,
                          BigDecimal debit, BigDecimal credit, String referenceSource) {}

    public record Balance(String compteCode, String compteLibelle, BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal solde) {}
}
