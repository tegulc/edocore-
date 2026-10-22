package com.educore.modules.paiement.controller;

import com.educore.modules.paiement.dto.WebhookPaiementDto;
import com.educore.modules.paiement.service.PaiementService;
import com.educore.modules.paiement.service.WebhookSignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhooks des fournisseurs de paiement mobile (Volume 6, Partie V). Public
 * (voir SecurityConfig) : aucun JWT n'est attendu ici, l'authenticite de
 * l'appelant repose exclusivement sur la signature HMAC (Regle de securite :
 * le corps est lu en chaine BRUTE pour verifier la signature AVANT tout
 * parsing JSON, sinon la signature calculee ne correspondrait plus a rien).
 * Idempotence : voir PaiementService.traiterWebhook.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks paiement", description = "Confirmations asynchrones Orange Money / MTN MoMo")
public class WebhookController {

    private final PaiementService paiementService;
    private final WebhookSignatureService webhookSignatureService;
    private final ObjectMapper objectMapper;

    public WebhookController(PaiementService paiementService, WebhookSignatureService webhookSignatureService, ObjectMapper objectMapper) {
        this.paiementService = paiementService;
        this.webhookSignatureService = webhookSignatureService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/orange-money")
    @Operation(summary = "Confirmation de paiement Orange Money (signature X-Signature verifiee)")
    public ResponseEntity<Void> orangeMoney(@RequestHeader("X-Signature") String signature, @RequestBody String corpsBrut) throws Exception {
        if (!webhookSignatureService.verifierOrangeMoney(corpsBrut, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // volontairement sans detail sur la cause exacte
        }
        traiter(corpsBrut);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mtn-momo")
    @Operation(summary = "Confirmation de paiement MTN MoMo (signature X-Signature verifiee)")
    public ResponseEntity<Void> mtnMomo(@RequestHeader("X-Signature") String signature, @RequestBody String corpsBrut) throws Exception {
        if (!webhookSignatureService.verifierMtnMomo(corpsBrut, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        traiter(corpsBrut);
        return ResponseEntity.ok().build();
    }

    private void traiter(String corpsBrut) throws Exception {
        WebhookPaiementDto.Confirmation confirmation = objectMapper.readValue(corpsBrut, WebhookPaiementDto.Confirmation.class);
        Long paiementId = Long.valueOf(confirmation.referenceMarchand());
        paiementService.traiterWebhook(paiementId, confirmation.referenceTransaction(), confirmation.statut());
    }
}
