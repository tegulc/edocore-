package com.educore.modules.emploidutemps.service;

import com.educore.modules.emploidutemps.dto.CreneauHoraireDto;
import com.educore.modules.emploidutemps.entity.CreneauHoraire;
import com.educore.modules.emploidutemps.repository.CreneauHoraireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreneauHoraireService {
    private final CreneauHoraireRepository creneauHoraireRepository;

    public CreneauHoraireService(CreneauHoraireRepository creneauHoraireRepository) {
        this.creneauHoraireRepository = creneauHoraireRepository;
    }

    @Transactional
    public CreneauHoraireDto.Profil creer(Long etablissementId, CreneauHoraireDto.Creation requete) {
        CreneauHoraire creneau = new CreneauHoraire();
        creneau.setEtablissementId(etablissementId);
        creneau.setLibelle(requete.libelle());
        creneau.setHeureDebut(requete.heureDebut());
        creneau.setHeureFin(requete.heureFin());
        creneau.setOrdre(requete.ordre());
        creneauHoraireRepository.save(creneau);
        return versProfil(creneau);
    }

    public List<CreneauHoraireDto.Profil> lister(Long etablissementId) {
        return creneauHoraireRepository.findByEtablissementIdOrderByOrdreAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    private CreneauHoraireDto.Profil versProfil(CreneauHoraire c) {
        return new CreneauHoraireDto.Profil(c.getId(), c.getLibelle(), c.getHeureDebut(), c.getHeureFin(), c.getOrdre());
    }
}
