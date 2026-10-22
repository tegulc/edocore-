package com.educore.modules.paiement.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Meme reserve que OrangeMoneyAdapter : non connecte a l'API MTN MoMo Collections reelle dans cet environnement. */
@Component
public class MtnMomoAdapter implements ProviderPaiement {

    @Override
    public String code() {
        return "mtn_momo";
    }

    @Override
    public String initier(String numeroTelephone, BigDecimal montant, String referenceMarchand) {
        // TODO production : POST vers l'API MTN MoMo Collections (educore.integrations.mtn-momo.api-key)
        return "Confirmez la transaction sur votre telephone.";
    }
}
