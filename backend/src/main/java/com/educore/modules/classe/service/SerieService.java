package com.educore.modules.classe.service;

import com.educore.modules.classe.dto.SerieDto;
import com.educore.modules.classe.entity.Serie;
import com.educore.modules.classe.repository.SerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SerieService {
    private final SerieRepository serieRepository;

    public SerieService(SerieRepository serieRepository) { this.serieRepository = serieRepository; }

    @Transactional
    public SerieDto.Profil creer(Long etablissementId, SerieDto.Creation requete) {
        Serie serie = new Serie();
        serie.setEtablissementId(etablissementId);
        serie.setCode(requete.code());
        serie.setLibelle(requete.libelle());
        if (requete.sousSerieCode() != null) serie.setSousSerieCode(requete.sousSerieCode());
        if (requete.serieParentId() != null) serie.setSerieParentId(requete.serieParentId());
        if (requete.sousSystemeId() != null) serie.setSousSystemeId(requete.sousSystemeId());
        if (requete.orientation() != null) serie.setOrientation(requete.orientation());
        if (requete.description() != null) serie.setDescription(requete.description());
        serieRepository.save(serie);
        return versProfil(serie);
    }

    public List<SerieDto.Profil> lister(Long etablissementId) {
        return serieRepository.findByEtablissementIdOrderByLibelleAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    private SerieDto.Profil versProfil(Serie s) {
        return new SerieDto.Profil(s.getId(), s.getCode(), s.getSousSerieCode(), s.getLibelle(),
                s.getSerieParentId(), s.getSousSystemeId(), s.getOrientation(), s.getDescription());
    }
}
