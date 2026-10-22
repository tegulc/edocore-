package com.educore.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * Point d'entree unique pour journaliser une action metier sensible.
 * Chaque module (utilisateurs, notes, paiements, ...) appelle
 * enregistrer(...) apres une creation/modification/suppression significative,
 * plutot que d'ecrire directement dans JournalAuditRepository — garantit un
 * format homogene (voir document d'architecture section 8.5).
 *
 * Volontairement synchrone et simple en Phase 1 ; a faire evoluer vers un
 * traitement @Async (cf. section 3.5 du document d'architecture) des que le
 * volume d'ecriture le justifiera, sans changer la signature publique.
 */
@Service
public class JournalAuditService {

    private final JournalAuditRepository journalAuditRepository;
    private final ObjectMapper objectMapper;

    public JournalAuditService(JournalAuditRepository journalAuditRepository, ObjectMapper objectMapper) {
        this.journalAuditRepository = journalAuditRepository;
        this.objectMapper = objectMapper;
    }

    public void enregistrer(Long etablissementId, Long utilisateurId, String action, String tableCible,
                             Long enregistrementId, Object avant, Object apres, String adresseIp) {
        JournalAudit entree = new JournalAudit();
        entree.setEtablissementId(etablissementId);
        entree.setUtilisateurId(utilisateurId);
        entree.setAction(action);
        entree.setTableCible(tableCible);
        entree.setEnregistrementId(enregistrementId);
        entree.setAnciennesValeurs(versJson(avant));
        entree.setNouvellesValeurs(versJson(apres));
        entree.setAdresseIp(adresseIp);
        journalAuditRepository.save(entree);
    }

    private String versJson(Object valeur) {
        if (valeur == null) return null;
        try {
            return objectMapper.writeValueAsString(valeur);
        } catch (Exception e) {
            return null; // l'audit ne doit jamais faire echouer l'operation metier principale
        }
    }
}
