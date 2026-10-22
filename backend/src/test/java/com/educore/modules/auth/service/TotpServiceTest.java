package com.educore.modules.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void genereUnSecretBase32Valide() {
        String secret = totpService.genererNouveauSecret();

        assertThat(secret).isNotBlank();
        assertThat(secret).matches("[A-Z2-7]+");
    }

    @Test
    void construitUneUriOtpauthConforme() {
        String secret = totpService.genererNouveauSecret();
        String uri = totpService.construireUriProvisionnement(secret, "directeur@lycee.cm");

        assertThat(uri).startsWith("otpauth://totp/EDUCORE:directeur@lycee.cm");
        assertThat(uri).contains("secret=" + secret);
        assertThat(uri).contains("issuer=EDUCORE");
    }

    @Test
    void refuseUnCodeAuFormatInvalide() {
        String secret = totpService.genererNouveauSecret();

        assertThat(totpService.verifier(secret, "abc")).isFalse();
        assertThat(totpService.verifier(secret, "12")).isFalse();
        assertThat(totpService.verifier(secret, null)).isFalse();
    }

    @Test
    void refuseUnCodeAleatoireNonGenereParLeSecret() {
        String secret = totpService.genererNouveauSecret();

        assertThat(totpService.verifier(secret, "000000")).isFalse();
    }
}
