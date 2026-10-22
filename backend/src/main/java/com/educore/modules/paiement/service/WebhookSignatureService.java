package com.educore.modules.paiement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Verifie la signature HMAC des webhooks entrants (Volume 6, Partie V.1 :
 * "chaque webhook est authentifie par signature HMAC calculee avec une cle
 * secrete partagee"). Comparaison en temps constant (MessageDigest.isEqual)
 * pour eviter une fuite d'information par timing attack.
 */
@Service
public class WebhookSignatureService {

    @Value("${educore.integrations.orange-money.webhook-secret:}")
    private String secretOrangeMoney;

    @Value("${educore.integrations.mtn-momo.webhook-secret:}")
    private String secretMtnMomo;

    public boolean verifierOrangeMoney(String corpsBrut, String signatureRecue) {
        return verifier(corpsBrut, signatureRecue, secretOrangeMoney);
    }

    public boolean verifierMtnMomo(String corpsBrut, String signatureRecue) {
        return verifier(corpsBrut, signatureRecue, secretMtnMomo);
    }

    private boolean verifier(String corpsBrut, String signatureRecue, String secret) {
        if (signatureRecue == null || secret == null || secret.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signatureCalculee = mac.doFinal(corpsBrut.getBytes(StandardCharsets.UTF_8));
            String signatureCalculeeHex = Base64.getEncoder().encodeToString(signatureCalculee);
            return java.security.MessageDigest.isEqual(
                    signatureCalculeeHex.getBytes(StandardCharsets.UTF_8),
                    signatureRecue.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
