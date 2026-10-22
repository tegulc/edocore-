package com.educore.modules.etablissement.service;

import com.educore.modules.etablissement.dto.VacanceScolaireDto;
import com.educore.modules.etablissement.entity.VacanceScolaire;
import com.educore.modules.etablissement.repository.VacanceScolaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VacanceScolaireService {

    private final VacanceScolaireRepository vacanceScolaireRepository;
    private final AnneeScolaireService anneeScolaireService;

    public VacanceScolaireService(VacanceScolaireRepository vacanceScolaireRepository, AnneeScolaireService anneeScolaireService) {
        this.vacanceScolaireRepository = vacanceScolaireRepository;
        this.anneeScolaireService = anneeScolaireService;
    }

    @Transactional
    public VacanceScolaireDto.Profil creer(Long etablissementId, Long anneeId, VacanceScolaireDto.Creation requete) {
        anneeScolaireService.trouverDansEtablissement(etablissementId, anneeId);

        VacanceScolaire vacance = new VacanceScolaire();
        vacance.setAnneeScolaireId(anneeId);
        vacance.setLibelle(requete.libelle());
        vacance.setDateDebut(requete.dateDebut());
        vacance.setDateFin(requete.dateFin());
        vacanceScolaireRepository.save(vacance);
        return versProfil(vacance);
    }

    public List<VacanceScolaireDto.Profil> lister(Long etablissementId, Long anneeId) {
        anneeScolaireService.trouverDansEtablissement(etablissementId, anneeId);
        return vacanceScolaireRepository.findByAnneeScolaireIdOrderByDateDebutAsc(anneeId).stream().map(this::versProfil).toList();
    }

    private VacanceScolaireDto.Profil versProfil(VacanceScolaire v) {
        return new VacanceScolaireDto.Profil(v.getId(), v.getLibelle(), v.getDateDebut(), v.getDateFin());
    }
}
