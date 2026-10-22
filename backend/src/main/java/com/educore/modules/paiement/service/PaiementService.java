package com.educore.modules.paiement.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.comptabilite.service.EcritureComptableService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.paiement.adapter.ProviderPaiement;
import com.educore.modules.paiement.dto.PaiementDto;
import com.educore.modules.paiement.entity.CleIdempotence;
import com.educore.modules.paiement.entity.MoyenPaiement;
import com.educore.modules.paiement.entity.Paiement;
import com.educore.modules.paiement.entity.TranchePaiement;
import com.educore.modules.paiement.repository.CleIdempotenceRepository;
import com.educore.modules.paiement.repository.MoyenPaiementRepository;
import com.educore.modules.paiement.repository.PaiementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Paiements (Module 14). Deux entrees possibles : mobile money (initiation
 * asynchrone + confirmation par webhook, Volume 6 Parties IV.3/V) et
 * encaissement manuel immediat (especes/cheque au guichet, confirme sur le
 * champ). Regle 01 : idempotence stricte sur Idempotency-Key (rejoue = meme
 * reponse, jamais un second paiement). Regle 02 : idempotence stricte sur
 * reference_transaction cote webhook (meme regle, cote fournisseur).
 */
@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final MoyenPaiementRepository moyenPaiementRepository;
    private final CleIdempotenceRepository cleIdempotenceRepository;
    private final EcheancierService echeancierService;
    private final RecuService recuService;
    private final EleveService eleveService;
    private final EcritureComptableService ecritureComptableService;
    private final List<ProviderPaiement> fournisseurs;

    public PaiementService(PaiementRepository paiementRepository, MoyenPaiementRepository moyenPaiementRepository,
                            CleIdempotenceRepository cleIdempotenceRepository, EcheancierService echeancierService,
                            RecuService recuService, EleveService eleveService, EcritureComptableService ecritureComptableService,
                            List<ProviderPaiement> fournisseurs) {
        this.paiementRepository = paiementRepository;
        this.moyenPaiementRepository = moyenPaiementRepository;
        this.cleIdempotenceRepository = cleIdempotenceRepository;
        this.echeancierService = echeancierService;
        this.recuService = recuService;
        this.eleveService = eleveService;
        this.ecritureComptableService = ecritureComptableService;
        this.fournisseurs = fournisseurs;
    }

    @Transactional
    public PaiementDto.InitiationReponse initier(Long etablissementId, String cleIdempotence, PaiementDto.Initiation requete) {
        var cleExistante = cleIdempotenceRepository.findByEtablissementIdAndCle(etablissementId, cleIdempotence);
        if (cleExistante.isPresent()) {
            Paiement paiementExistant = paiementRepository.findById(cleExistante.get().getPaiementId())
                    .orElseThrow(() -> new IllegalStateException("Cle d'idempotence orpheline"));
            return new PaiementDto.InitiationReponse(paiementExistant.getId(), paiementExistant.getStatut(),
                    "Confirmez la transaction sur votre telephone.");
        }

        eleveService.trouver(etablissementId, requete.eleveId());
        MoyenPaiement moyen = moyenPaiementRepository.findByCode(requete.moyenPaiement())
                .orElseThrow(() -> new BusinessException("MOYEN_PAIEMENT_INCONNU", "Moyen de paiement inconnu : " + requete.moyenPaiement(), HttpStatus.BAD_REQUEST));

        ProviderPaiement fournisseur = fournisseurs.stream()
                .filter(f -> f.code().equals(moyen.getCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("MOYEN_NON_SUPPORTE", "Ce moyen de paiement necessite un encaissement manuel.", HttpStatus.BAD_REQUEST));

        Paiement paiement = new Paiement();
        paiement.setEtablissementId(etablissementId);
        paiement.setEleveId(requete.eleveId());
        paiement.setTrancheId(requete.trancheId());
        paiement.setMoyenPaiementId(moyen.getId());
        paiement.setMontant(requete.montant());
        paiement.setStatut("en_attente");
        paiementRepository.save(paiement);

        CleIdempotence cle = new CleIdempotence();
        cle.setEtablissementId(etablissementId);
        cle.setCle(cleIdempotence);
        cle.setPaiementId(paiement.getId());
        cleIdempotenceRepository.save(cle);

        String instructions = fournisseur.initier(requete.numeroTelephone(), requete.montant(), String.valueOf(paiement.getId()));

        return new PaiementDto.InitiationReponse(paiement.getId(), paiement.getStatut(), instructions);
    }

    /** Enregistrement immediat (especes/cheque au guichet) - confirme directement, pas de webhook attendu. */
    @Transactional
    public PaiementDto.Profil encaisser(Long etablissementId, Long principalId, PaiementDto.Encaissement requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        MoyenPaiement moyen = moyenPaiementRepository.findByCode(requete.moyenPaiement())
                .orElseThrow(() -> new BusinessException("MOYEN_PAIEMENT_INCONNU", "Moyen de paiement inconnu : " + requete.moyenPaiement(), HttpStatus.BAD_REQUEST));

        Paiement paiement = new Paiement();
        paiement.setEtablissementId(etablissementId);
        paiement.setEleveId(requete.eleveId());
        paiement.setTrancheId(requete.trancheId());
        paiement.setMoyenPaiementId(moyen.getId());
        paiement.setMontant(requete.montant());
        paiement.setStatut("confirme");
        paiement.setEncaisseParId(principalId);
        paiementRepository.save(paiement);

        finaliserPaiementConfirme(etablissementId, paiement);
        return versProfil(paiement, moyen.getCode());
    }

    /**
     * Traite un webhook de confirmation (Volume 6, Partie V). Idempotent sur
     * referenceTransaction : un meme evenement recu plusieurs fois (retry
     * reseau cote operateur) ne confirme le paiement qu'une seule fois.
     */
    @Transactional
    public void traiterWebhook(Long paiementId, String referenceTransaction, String statutFournisseur) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceIntrouvableException("Paiement", paiementId));

        if ("confirme".equals(paiement.getStatut())) {
            return; // deja traite (idempotence) : ne rejoue jamais la confirmation
        }

        paiement.setReferenceTransaction(referenceTransaction);
        if ("SUCCESS".equalsIgnoreCase(statutFournisseur)) {
            paiement.setStatut("confirme");
            finaliserPaiementConfirme(paiement.getEtablissementId(), paiement);
        } else {
            paiement.setStatut("echoue");
        }
    }

    private void finaliserPaiementConfirme(Long etablissementId, Paiement paiement) {
        if (paiement.getTrancheId() != null) {
            TranchePaiement tranche = echeancierService.trouverTranche(paiement.getTrancheId());
            echeancierService.recalculerStatutTranche(tranche);
        }
        recuService.genererPour(paiement);

        ecritureComptableService.enregistrerPaireAutomatique(etablissementId, LocalDate.now(),
                "Paiement scolarite - eleve #%d".formatted(paiement.getEleveId()), paiement.getMontant(),
                "512", "Tresorerie", "706", "Produits de scolarite", "paiement:" + paiement.getId());
    }

    /** Utilise par le Module Dashboard pour l'indicateur "paiements du mois". */
    public java.math.BigDecimal sommeConfirmeeCeMois(Long etablissementId) {
        var maintenant = java.time.LocalDate.now();
        var debutMois = maintenant.withDayOfMonth(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        var debutMoisSuivant = maintenant.withDayOfMonth(1).plusMonths(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        return paiementRepository.sommeConfirmeeSurPeriode(etablissementId, debutMois, debutMoisSuivant);
    }

    public List<PaiementDto.Profil> historique(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        Map<Long, String> codesMoyens = moyenPaiementRepository.findAll().stream()
                .collect(Collectors.toMap(MoyenPaiement::getId, MoyenPaiement::getCode));
        return paiementRepository.findByEleveIdOrderByDatePaiementDesc(eleveId).stream()
                .map(p -> versProfil(p, codesMoyens.get(p.getMoyenPaiementId())))
                .toList();
    }

    private PaiementDto.Profil versProfil(Paiement p, String moyenCode) {
        return new PaiementDto.Profil(p.getId(), p.getUuid().toString(), p.getEleveId(), p.getTrancheId(), moyenCode,
                p.getMontant(), p.getReferenceTransaction(), p.getStatut(),
                DateTimeFormatter.ISO_INSTANT.format(p.getDatePaiement()));
    }
}
