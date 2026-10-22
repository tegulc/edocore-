package com.educore.modules.classe.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.classe.dto.NiveauDto;
import com.educore.modules.classe.entity.Niveau;
import com.educore.modules.classe.repository.NiveauRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NiveauService {
    private final NiveauRepository niveauRepository;

    public NiveauService(NiveauRepository niveauRepository) { this.niveauRepository = niveauRepository; }

    @Transactional
    public NiveauDto.Profil creer(Long etablissementId, NiveauDto.Creation requete) {
        if (niveauRepository.existsByEtablissementIdAndCode(etablissementId, requete.code())) {
            throw new BusinessException("NIVEAU_DEJA_EXISTANT", "Ce niveau existe deja.", HttpStatus.CONFLICT);
        }
        Niveau niveau = new Niveau();
        niveau.setEtablissementId(etablissementId);
        niveau.setCode(requete.code());
        niveau.setLibelle(requete.libelle());
        niveau.setOrdre(requete.ordre());
        niveau.setCycle(requete.cycle());
        niveauRepository.save(niveau);
        return versProfil(niveau);
    }

    public List<NiveauDto.Profil> lister(Long etablissementId) {
        return niveauRepository.findByEtablissementIdOrderByOrdreAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    private NiveauDto.Profil versProfil(Niveau n) {
        return new NiveauDto.Profil(n.getId(), n.getCode(), n.getLibelle(), n.getOrdre(), n.getCycle());
    }
}
