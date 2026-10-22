package com.educore.modules.classe.service;

import com.educore.modules.classe.dto.SalleDto;
import com.educore.modules.classe.entity.Salle;
import com.educore.modules.classe.repository.SalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalleService {
    private final SalleRepository salleRepository;

    public SalleService(SalleRepository salleRepository) { this.salleRepository = salleRepository; }

    @Transactional
    public SalleDto.Profil creer(Long etablissementId, SalleDto.Creation requete) {
        Salle salle = new Salle();
        salle.setEtablissementId(etablissementId);
        salle.setNom(requete.nom());
        salle.setCapacite(requete.capacite());
        if (requete.typeSalle() != null) salle.setTypeSalle(requete.typeSalle());
        salleRepository.save(salle);
        return versProfil(salle);
    }

    public List<SalleDto.Profil> lister(Long etablissementId) {
        return salleRepository.findByEtablissementIdOrderByNomAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    private SalleDto.Profil versProfil(Salle s) {
        return new SalleDto.Profil(s.getId(), s.getNom(), s.getCapacite(), s.getTypeSalle());
    }
}
