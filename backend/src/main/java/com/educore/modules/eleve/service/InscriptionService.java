package com.educore.modules.eleve.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.dto.ClasseDto;
import com.educore.modules.classe.service.ClasseService;
import com.educore.modules.eleve.dto.InscriptionDto;
import com.educore.modules.eleve.entity.Inscription;
import com.educore.modules.eleve.repository.InscriptionRepository;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inscription d'un eleve dans une classe pour l'annee scolaire ACTIVE
 * (une ligne par annee = reinscription geree naturellement, cf. commentaire
 * du script SQL). Regle 01 : jamais plus d'une inscription validee par
 * (eleve, annee). Regle 02 : jamais au-dela de la capacite de la classe.
 */
@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EleveService eleveService;
    private final ClasseService classeService;
    private final AnneeScolaireService anneeScolaireService;

    public InscriptionService(InscriptionRepository inscriptionRepository, EleveService eleveService,
                               ClasseService classeService, AnneeScolaireService anneeScolaireService) {
        this.inscriptionRepository = inscriptionRepository;
        this.eleveService = eleveService;
        this.classeService = classeService;
        this.anneeScolaireService = anneeScolaireService;
    }

    @Transactional
    public InscriptionDto.Profil inscrire(Long etablissementId, Long eleveId, InscriptionDto.Creation requete) {
        eleveService.trouver(etablissementId, eleveId); // verifie l'appartenance au tenant
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();

        if (inscriptionRepository.findByEleveIdAndAnneeScolaireId(eleveId, anneeActiveId).isPresent()) {
            throw new BusinessException("DEJA_INSCRIT", "Cet eleve est deja inscrit pour l'annee scolaire active.", HttpStatus.CONFLICT);
        }

        ClasseDto.Profil classe = classeService.obtenir(etablissementId, requete.classeId());
        if (classe.effectif() >= classe.capacite()) {
            throw new BusinessException("CLASSE_COMPLETE",
                    "Cette classe a atteint sa capacite maximale (%d/%d).".formatted(classe.effectif(), classe.capacite()),
                    HttpStatus.CONFLICT);
        }

        Inscription inscription = new Inscription();
        inscription.setEleveId(eleveId);
        inscription.setAnneeScolaireId(anneeActiveId);
        inscription.setClasseId(classe.id());
        if (requete.typeInscription() != null) inscription.setTypeInscription(requete.typeInscription());
        inscription.setStatut("validee");
        inscriptionRepository.save(inscription);

        return versProfil(inscription, classe.libelle());
    }

    public List<InscriptionDto.Profil> historique(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return inscriptionRepository.findByEleveIdOrderByAnneeScolaireIdDesc(eleveId).stream()
                .map(i -> versProfil(i, classeService.obtenir(etablissementId, i.getClasseId()).libelle()))
                .toList();
    }

    /** Utilise par le module Notes (MoyenneCalculService) pour calculer les moyennes de tous les eleves d'une classe. */
    public List<Long> listerElevesInscrits(Long classeId) {
        return inscriptionRepository.findByClasseIdAndStatut(classeId, "validee").stream()
                .map(com.educore.modules.eleve.entity.Inscription::getEleveId)
                .toList();
    }

    private InscriptionDto.Profil versProfil(Inscription i, String classeLibelle) {
        return new InscriptionDto.Profil(i.getId(), i.getEleveId(), i.getAnneeScolaireId(), i.getClasseId(),
                classeLibelle, i.getTypeInscription(), i.getStatut());
    }
}
