package com.educore.modules.paiement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrancheDto {
    public record Profil(Long id, Long echeancierId, int numeroTranche, BigDecimal montantDu, BigDecimal montantPaye,
                          LocalDate dateEcheance, String statut) {}
}
