package com.educore.modules.paiement.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.paiement.dto.EcheancierDto;
import com.educore.modules.paiement.dto.TrancheDto;
import com.educore.modules.paiement.entity.EcheancierPaiement;
import com.educore.modules.paiement.entity.StructureFrais;
import com.educore.modules.paiement.entity.TranchePaiement;
import com.educore.modules.paiement.repository.EcheancierPaiementRepository;
import com.educore.modules.paiement.repository.PaiementRepository;
import com.educore.modules.paiement.repository.TranchePaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Genere l'echeancier de paiement d'un eleve a partir d'une grille tarifaire, reparti en N tranches egales mensuelles. */
@Service
public class EcheancierService {

    private final EcheancierPaiementRepository echeancierPaiementRepository;
    private final TranchePaiementRepository tranchePaiementRepository;
    private final PaiementRepository paiementRepository;
    private final StructureFraisService structureFraisService;
    private final EleveService eleveService;

    public EcheancierService(EcheancierPaiementRepository echeancierPaiementRepository, TranchePaiementRepository tranchePaiementRepository,
                              PaiementRepository paiementRepository, StructureFraisService structureFraisService, EleveService eleveService) {
        this.echeancierPaiementRepository = echeancierPaiementRepository;
        this.tranchePaiementRepository = tranchePaiementRepository;
        this.paiementRepository = paiementRepository;
        this.structureFraisService = structureFraisService;
        this.eleveService = eleveService;
    }

    @Transactional
    public EcheancierDto.Profil genererPourEleve(Long etablissementId, Long eleveId, EcheancierDto.Generation requete) {
        eleveService.trouver(etablissementId, eleveId);
        StructureFrais structure = structureFraisService.trouver(etablissementId, requete.structureFraisId());

        EcheancierPaiement echeancier = new EcheancierPaiement();
        echeancier.setEleveId(eleveId);
        echeancier.setStructureFraisId(structure.getId());
        echeancier.setMontantTotal(structure.getMontant());
        echeancier.setNombreTranches(requete.nombreTranches());
        echeancierPaiementRepository.save(echeancier);

        BigDecimal montantParTranche = structure.getMontant().divide(BigDecimal.valueOf(requete.nombreTranches()), 2, RoundingMode.HALF_UP);
        for (int i = 1; i <= requete.nombreTranches(); i++) {
            TranchePaiement tranche = new TranchePaiement();
            tranche.setEcheancierId(echeancier.getId());
            tranche.setNumeroTranche(i);
            tranche.setMontantDu(montantParTranche);
            tranche.setDateEcheance(requete.premiereEcheance().plusMonths(i - 1L));
            tranchePaiementRepository.save(tranche);
        }

        return versProfil(echeancier);
    }

    public List<EcheancierDto.Profil> listerPourEleve(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return echeancierPaiementRepository.findByEleveId(eleveId).stream().map(this::versProfil).toList();
    }

    TranchePaiement trouverTranche(Long trancheId) {
        return tranchePaiementRepository.findById(trancheId)
                .orElseThrow(() -> new RessourceIntrouvableException("TranchePaiement", trancheId));
    }

    /** Recalcule le statut d'une tranche (impaye/partiel/paye) a partir des paiements confirmes. Appele apres chaque paiement confirme. */
    @Transactional
    void recalculerStatutTranche(TranchePaiement tranche) {
        BigDecimal paye = paiementRepository.sommePayeeSurTranche(tranche.getId());
        if (paye.compareTo(tranche.getMontantDu()) >= 0) {
            tranche.setStatut("paye");
        } else if (paye.compareTo(BigDecimal.ZERO) > 0) {
            tranche.setStatut("partiel");
        } else if (tranche.getDateEcheance().isBefore(LocalDate.now())) {
            tranche.setStatut("en_retard");
        }
        tranchePaiementRepository.save(tranche);
    }

    private EcheancierDto.Profil versProfil(EcheancierPaiement e) {
        List<TrancheDto.Profil> tranches = tranchePaiementRepository.findByEcheancierIdOrderByNumeroTrancheAsc(e.getId()).stream()
                .map(t -> new TrancheDto.Profil(t.getId(), t.getEcheancierId(), t.getNumeroTranche(), t.getMontantDu(),
                        paiementRepository.sommePayeeSurTranche(t.getId()), t.getDateEcheance(), t.getStatut()))
                .toList();
        return new EcheancierDto.Profil(e.getId(), e.getEleveId(), e.getStructureFraisId(), e.getMontantTotal(), e.getNombreTranches(), tranches);
    }
}
