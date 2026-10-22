package com.educore.modules.paiement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Noms de champs conformes au Volume 6 (Documentation API), Partie IV.3, une fois convertis en snake_case sur le fil. */
public class PaiementDto {

    public record Initiation(@NotNull Long eleveId, Long trancheId, @NotNull @Positive BigDecimal montant,
                              @NotBlank String moyenPaiement, String numeroTelephone) {}

    /** Reponse 202 (traitement asynchrone) conforme a l'exemple exact du Volume 6 §4.3. */
    public record InitiationReponse(Long paiementId, String statut, String instructions) {}

    public record Profil(Long id, String uuid, Long eleveId, Long trancheId, String moyenPaiement, BigDecimal montant,
                          String referenceTransaction, String statut, String datePaiement) {}

    public record Encaissement(@NotNull Long eleveId, Long trancheId, @NotNull @Positive BigDecimal montant, @NotBlank String moyenPaiement) {}
}
