package com.educore.modules.paiement.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * IMPORTANT : implementation non connectee a l'API reelle Orange Money
 * (pas d'acces reseau ni de identifiants marchands dans cet environnement
 * de developpement). Simule une reponse d'initiation reussie pour permettre
 * de construire et tester tout le flux applicatif autour (idempotence,
 * webhook, reconciliation). Brancher le veritable appel HTTP vers l'API
 * Orange Money Web Payment ici avant la mise en production.
 */
@Component
public class OrangeMoneyAdapter implements ProviderPaiement {

    @Override
    public String code() {
        return "orange_money";
    }

    @Override
    public String initier(String numeroTelephone, BigDecimal montant, String referenceMarchand) {
        // TODO production : POST vers l'API Orange Money Web Payment (educore.integrations.orange-money.api-key)
        return "Confirmez la transaction sur votre telephone.";
    }
}
