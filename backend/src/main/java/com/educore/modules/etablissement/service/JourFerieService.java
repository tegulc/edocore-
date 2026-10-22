package com.educore.modules.etablissement.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.etablissement.dto.JourFerieDto;
import com.educore.modules.etablissement.entity.JourFerie;
import com.educore.modules.etablissement.repository.JourFerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JourFerieService {

    private final JourFerieRepository jourFerieRepository;

    public JourFerieService(JourFerieRepository jourFerieRepository) {
        this.jourFerieRepository = jourFerieRepository;
    }

    @Transactional
    public JourFerieDto.Profil creer(Long etablissementId, JourFerieDto.Creation requete) {
        JourFerie jour = new JourFerie();
        jour.setEtablissementId(etablissementId);
        jour.setLibelle(requete.libelle());
        jour.setDateJour(requete.dateJour());
        jour.setRecurrentAnnuel(requete.recurrentAnnuel());
        jourFerieRepository.save(jour);
        return versProfil(jour);
    }

    public List<JourFerieDto.Profil> lister(Long etablissementId) {
        return jourFerieRepository.findByEtablissementIdOrderByDateJourAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    @Transactional
    public void supprimer(Long etablissementId, Long id) {
        JourFerie jour = jourFerieRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("JourFerie", id));
        if (!jour.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("JourFerie", id);
        }
        jourFerieRepository.delete(jour);
    }

    private JourFerieDto.Profil versProfil(JourFerie j) {
        return new JourFerieDto.Profil(j.getId(), j.getLibelle(), j.getDateJour(), j.isRecurrentAnnuel());
    }
}
