package com.educore.modules.emploidutemps.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.classe.service.ClasseService;
import com.educore.modules.classe.service.MatiereService;
import com.educore.modules.emploidutemps.dto.EmploiDuTempsDto;
import com.educore.modules.emploidutemps.entity.EmploiDuTemps;
import com.educore.modules.emploidutemps.repository.EmploiDuTempsRepository;
import com.educore.modules.enseignant.service.EnseignantService;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Grille horaire hebdomadaire recurrente. Regle 01 : jamais un enseignant, une
 * salle ou une classe reserves deux fois sur le meme creneau/jour pour la
 * meme annee (deja impose par index unique en base - voir V1 - revalide ici
 * pour un message d'erreur clair plutot qu'une violation SQL brute).
 */
@Service
public class EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final ClasseService classeService;
    private final MatiereService matiereService;
    private final EnseignantService enseignantService;
    private final AnneeScolaireService anneeScolaireService;

    public EmploiDuTempsService(EmploiDuTempsRepository emploiDuTempsRepository, ClasseService classeService,
                                 MatiereService matiereService, EnseignantService enseignantService,
                                 AnneeScolaireService anneeScolaireService) {
        this.emploiDuTempsRepository = emploiDuTempsRepository;
        this.classeService = classeService;
        this.matiereService = matiereService;
        this.enseignantService = enseignantService;
        this.anneeScolaireService = anneeScolaireService;
    }

    @Transactional
    public EmploiDuTempsDto.Profil creer(Long etablissementId, EmploiDuTempsDto.Creation requete) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();

        var classe = classeService.obtenir(etablissementId, requete.classeId());
        var matiere = matiereService.obtenir(etablissementId, requete.matiereId());
        var enseignant = enseignantService.obtenir(etablissementId, requete.enseignantId());

        if (emploiDuTempsRepository.existsByEnseignantIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(
                requete.enseignantId(), requete.creneauId(), requete.jourSemaine(), anneeActiveId)) {
            throw new BusinessException("ENSEIGNANT_DEJA_OCCUPE", "Cet enseignant a deja cours sur ce creneau ce jour-la.", HttpStatus.CONFLICT);
        }
        if (emploiDuTempsRepository.existsByClasseIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(
                requete.classeId(), requete.creneauId(), requete.jourSemaine(), anneeActiveId)) {
            throw new BusinessException("CLASSE_DEJA_OCCUPEE", "Cette classe a deja cours sur ce creneau ce jour-la.", HttpStatus.CONFLICT);
        }
        if (requete.salleId() != null && emploiDuTempsRepository.existsBySalleIdAndCreneauIdAndJourSemaineAndAnneeScolaireId(
                requete.salleId(), requete.creneauId(), requete.jourSemaine(), anneeActiveId)) {
            throw new BusinessException("SALLE_DEJA_OCCUPEE", "Cette salle est deja reservee sur ce creneau ce jour-la.", HttpStatus.CONFLICT);
        }

        EmploiDuTemps seance = new EmploiDuTemps();
        seance.setEtablissementId(etablissementId);
        seance.setClasseId(classe.id());
        seance.setMatiereId(matiere.id());
        seance.setEnseignantId(enseignant.id());
        seance.setSalleId(requete.salleId());
        seance.setCreneauId(requete.creneauId());
        seance.setJourSemaine(requete.jourSemaine());
        seance.setAnneeScolaireId(anneeActiveId);
        emploiDuTempsRepository.save(seance);

        return versProfil(seance, matiere.libelle(), enseignant.utilisateur().prenom() + " " + enseignant.utilisateur().nom(), null);
    }

    public List<EmploiDuTempsDto.Profil> parClasse(Long etablissementId, Long classeId) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();
        return emploiDuTempsRepository.findByEtablissementIdAndAnneeScolaireIdAndClasseId(etablissementId, anneeActiveId, classeId).stream()
                .map(this::enrichir).toList();
    }

    public List<EmploiDuTempsDto.Profil> parEnseignant(Long etablissementId, Long enseignantId) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();
        return emploiDuTempsRepository.findByEtablissementIdAndAnneeScolaireIdAndEnseignantId(etablissementId, anneeActiveId, enseignantId).stream()
                .map(this::enrichir).toList();
    }

    private EmploiDuTempsDto.Profil enrichir(EmploiDuTemps s) {
        var matiere = matiereService.obtenir(s.getEtablissementId(), s.getMatiereId());
        var enseignant = enseignantService.obtenir(s.getEtablissementId(), s.getEnseignantId());
        return versProfil(s, matiere.libelle(), enseignant.utilisateur().prenom() + " " + enseignant.utilisateur().nom(), null);
    }

    private EmploiDuTempsDto.Profil versProfil(EmploiDuTemps s, String matiereLibelle, String enseignantNom, String creneauLibelle) {
        return new EmploiDuTempsDto.Profil(s.getId(), s.getClasseId(), s.getMatiereId(), matiereLibelle,
                s.getEnseignantId(), enseignantNom, s.getSalleId(), s.getCreneauId(), creneauLibelle, s.getJourSemaine());
    }
}
