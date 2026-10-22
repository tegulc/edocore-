package com.educore.modules.paiement.adapter;

import java.math.BigDecimal;

/**
 * Contrat commun a tous les fournisseurs de paiement mobile (Volume 3,
 * Partie IV.10.3 : "toute integration externe passe par une couche
 * d'adaptateur dediee, pour ne jamais coupler la logique metier a un
 * fournisseur particulier"). Ajouter un nouveau moyen de paiement = ajouter
 * une implementation, jamais modifier PaiementService.
 */
public interface ProviderPaiement {

    String code(); // "orange_money", "mtn_momo"...

    /**
     * Demande a l'operateur de solliciter une confirmation cote client
     * (USSD/app). Retourne des instructions lisibles a afficher tel quel
     * (voir Volume 6 §4.3 : "Confirmez la transaction sur votre telephone.").
     */
    String initier(String numeroTelephone, BigDecimal montant, String referenceMarchand);
}
