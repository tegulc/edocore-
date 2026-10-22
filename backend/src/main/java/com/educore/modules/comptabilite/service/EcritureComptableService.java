package com.educore.modules.comptabilite.service;

import com.educore.modules.comptabilite.dto.EcritureComptableDto;
import com.educore.modules.comptabilite.entity.CompteComptable;
import com.educore.modules.comptabilite.entity.EcritureComptable;
import com.educore.modules.comptabilite.repository.CompteComptableRepository;
import com.educore.modules.comptabilite.repository.EcritureComptableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Journal comptable en partie double (Module 15). Chaque operation financiere
 * automatisee (paiement confirme, salaire...) genere deux ecritures liees
 * par la meme reference_source : un debit et un credit de meme montant, sur
 * deux comptes differents, conformement au principe de la partie double.
 */
@Service
public class EcritureComptableService {

    private final EcritureComptableRepository ecritureComptableRepository;
    private final CompteComptableRepository compteComptableRepository;

    public EcritureComptableService(EcritureComptableRepository ecritureComptableRepository, CompteComptableRepository compteComptableRepository) {
        this.ecritureComptableRepository = ecritureComptableRepository;
        this.compteComptableRepository = compteComptableRepository;
    }

    @Transactional
    public EcritureComptableDto.Profil enregistrer(Long etablissementId, Long principalId, EcritureComptableDto.Creation requete) {
        EcritureComptable ecriture = construireEcriture(etablissementId, requete.compteId(), requete.dateEcriture(),
                requete.libelle(), requete.debit(), requete.credit(), requete.pieceJustificativeUrl(), null, principalId);
        ecritureComptableRepository.save(ecriture);
        return versProfil(ecriture);
    }

    /** Utilise par PaiementService : genere automatiquement la double ecriture (debit tresorerie / credit produits scolarite). */
    @Transactional
    public void enregistrerPaireAutomatique(Long etablissementId, LocalDate date, String libelle, BigDecimal montant,
                                             String codeCompteDebit, String libelleCompteDebit,
                                             String codeCompteCredit, String libelleCompteCredit, String referenceSource) {
        CompteComptable compteDebit = obtenirOuCreer(etablissementId, codeCompteDebit, libelleCompteDebit, "actif");
        CompteComptable compteCredit = obtenirOuCreer(etablissementId, codeCompteCredit, libelleCompteCredit, "produit");

        EcritureComptable debit = construireEcriture(etablissementId, compteDebit.getId(), date, libelle, montant, BigDecimal.ZERO, null, referenceSource, null);
        EcritureComptable credit = construireEcriture(etablissementId, compteCredit.getId(), date, libelle, BigDecimal.ZERO, montant, null, referenceSource, null);
        ecritureComptableRepository.save(debit);
        ecritureComptableRepository.save(credit);
    }

    public List<EcritureComptableDto.Profil> lister(Long etablissementId, LocalDate debut, LocalDate fin) {
        return ecritureComptableRepository.findByEtablissementIdAndDateEcritureBetweenOrderByDateEcritureDesc(etablissementId, debut, fin)
                .stream().map(this::versProfil).toList();
    }

    public List<EcritureComptableDto.Balance> balance(Long etablissementId, LocalDate debut, LocalDate fin) {
        Map<Long, CompteComptable> comptes = compteComptableRepository.findByEtablissementId(etablissementId).stream()
                .collect(Collectors.toMap(CompteComptable::getId, c -> c));

        return ecritureComptableRepository.findByEtablissementIdAndDateEcritureBetweenOrderByDateEcritureDesc(etablissementId, debut, fin).stream()
                .collect(Collectors.groupingBy(EcritureComptable::getCompteId))
                .entrySet().stream()
                .map(entree -> {
                    CompteComptable compte = comptes.get(entree.getKey());
                    BigDecimal totalDebit = entree.getValue().stream().map(EcritureComptable::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalCredit = entree.getValue().stream().map(EcritureComptable::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new EcritureComptableDto.Balance(compte != null ? compte.getCode() : "?", compte != null ? compte.getLibelle() : "?",
                            totalDebit, totalCredit, totalDebit.subtract(totalCredit));
                })
                .toList();
    }

    private CompteComptable obtenirOuCreer(Long etablissementId, String code, String libelle, String typeCompte) {
        return compteComptableRepository.findByEtablissementIdAndCode(etablissementId, code).orElseGet(() -> {
            CompteComptable nouveau = new CompteComptable();
            nouveau.setEtablissementId(etablissementId);
            nouveau.setCode(code);
            nouveau.setLibelle(libelle);
            nouveau.setTypeCompte(typeCompte);
            return compteComptableRepository.save(nouveau);
        });
    }

    private EcritureComptable construireEcriture(Long etablissementId, Long compteId, LocalDate date, String libelle,
                                                  BigDecimal debit, BigDecimal credit, String pieceUrl, String referenceSource, Long saisiParId) {
        EcritureComptable ecriture = new EcritureComptable();
        ecriture.setEtablissementId(etablissementId);
        ecriture.setCompteId(compteId);
        ecriture.setDateEcriture(date);
        ecriture.setLibelle(libelle);
        ecriture.setDebit(debit != null ? debit : BigDecimal.ZERO);
        ecriture.setCredit(credit != null ? credit : BigDecimal.ZERO);
        ecriture.setPieceJustificativeUrl(pieceUrl);
        ecriture.setReferenceSource(referenceSource);
        ecriture.setSaisiParId(saisiParId);
        return ecriture;
    }

    private EcritureComptableDto.Profil versProfil(EcritureComptable e) {
        String codeCompte = compteComptableRepository.findById(e.getCompteId()).map(CompteComptable::getCode).orElse("?");
        return new EcritureComptableDto.Profil(e.getId(), e.getCompteId(), codeCompte, e.getDateEcriture(), e.getLibelle(),
                e.getDebit(), e.getCredit(), e.getReferenceSource());
    }
}
