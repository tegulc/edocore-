package com.educore.modules.comptabilite.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.comptabilite.dto.CompteComptableDto;
import com.educore.modules.comptabilite.entity.CompteComptable;
import com.educore.modules.comptabilite.repository.CompteComptableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompteComptableService {
    private final CompteComptableRepository compteComptableRepository;

    public CompteComptableService(CompteComptableRepository compteComptableRepository) {
        this.compteComptableRepository = compteComptableRepository;
    }

    @Transactional
    public CompteComptableDto.Profil creer(Long etablissementId, CompteComptableDto.Creation requete) {
        if (compteComptableRepository.findByEtablissementIdAndCode(etablissementId, requete.code()).isPresent()) {
            throw new BusinessException("COMPTE_DEJA_EXISTANT", "Ce code de compte existe deja.", HttpStatus.CONFLICT);
        }
        CompteComptable compte = new CompteComptable();
        compte.setEtablissementId(etablissementId);
        compte.setCode(requete.code());
        compte.setLibelle(requete.libelle());
        compte.setTypeCompte(requete.typeCompte());
        compteComptableRepository.save(compte);
        return versProfil(compte);
    }

    public List<CompteComptableDto.Profil> lister(Long etablissementId) {
        return compteComptableRepository.findByEtablissementId(etablissementId).stream().map(this::versProfil).toList();
    }

    /** Cree le compte s'il n'existe pas encore (utilise par les generateurs d'ecritures automatiques - paiements, salaires...). */
    @Transactional
    public CompteComptable obtenirOuCreer(Long etablissementId, String code, String libelle, String typeCompte) {
        return compteComptableRepository.findByEtablissementIdAndCode(etablissementId, code).orElseGet(() -> {
            CompteComptable nouveau = new CompteComptable();
            nouveau.setEtablissementId(etablissementId);
            nouveau.setCode(code);
            nouveau.setLibelle(libelle);
            nouveau.setTypeCompte(typeCompte);
            return compteComptableRepository.save(nouveau);
        });
    }

    private CompteComptableDto.Profil versProfil(CompteComptable c) {
        return new CompteComptableDto.Profil(c.getId(), c.getCode(), c.getLibelle(), c.getTypeCompte());
    }
}
