package com.educore.common.audit;

import java.time.Instant;

public final class JournalAuditDto {

    private JournalAuditDto() {}

    public record Profil(Long id, Long utilisateurId, String action, String tableCible, Long enregistrementId,
                          String adresseIp, Instant horodatage) {}
}
