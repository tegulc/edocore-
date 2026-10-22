package com.educore.modules.paiement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Charge utile conforme au Volume 6 (Documentation API), Partie V.1-5.2. */
public class WebhookPaiementDto {
    public record Confirmation(@NotBlank String referenceTransaction, @NotBlank String statut,
                                @NotNull BigDecimal montant, @NotBlank String referenceMarchand) {}
}
