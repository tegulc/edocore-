package com.educore.modules.pedagogie.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.pedagogie.dto.PedagogieDto;
import com.educore.modules.pedagogie.entity.Devoir;
import com.educore.modules.pedagogie.entity.DevoirRendu;
import com.educore.modules.pedagogie.repository.DevoirRenduRepository;
import com.educore.modules.pedagogie.repository.DevoirRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Module 11 — Gestion des devoirs en ligne. */
@Service
public class DevoirService {

    private final DevoirRepository devoirRepository;
    private final DevoirRenduRepository renduRepository;
    private final ClasseMatiereService classeMatiereService;
    private final EleveService eleveService;

    public DevoirService(DevoirRepository devoirRepository, DevoirRenduRepository renduRepository,
                          ClasseMatiereService classeMatiereService, EleveService eleveService) {
        this.devoirRepository = devoirRepository;
        this.renduRepository = renduRepository;
        this.classeMatiereService = classeMatiereService;
        this.eleveService = eleveService;
    }

    @Transactional
    public PedagogieDto.DevoirProfil creer(Long etablissementId, Long utilisateurId, PedagogieDto.DevoirCreation requete) {
        classeMatiereService.obtenir(etablissementId, requete.classeMatiereId());
        Devoir devoir = new Devoir();
        devoir.setClasseMatiereId(requete.classeMatiereId());
        devoir.setTitre(requete.titre());
        devoir.setDescription(requete.description());
        devoir.setDateLimite(requete.dateLimite());
        devoir.setFichierSujetUrl(requete.fichierSujetUrl());
        devoir.setCreatedById(utilisateurId);
        devoirRepository.save(devoir);
        return versProfil(devoir);
    }

    public List<PedagogieDto.DevoirProfil> lister(Long etablissementId, Long classeMatiereId) {
        classeMatiereService.obtenir(etablissementId, classeMatiereId);
        return devoirRepository.findByClasseMatiereIdOrderByDateLimiteDesc(classeMatiereId).stream()
                .map(this::versProfil).toList();
    }

    public PedagogieDto.DevoirProfil obtenir(Long etablissementId, Long id) {
        Devoir devoir = trouver(id);
        classeMatiereService.obtenir(etablissementId, devoir.getClasseMatiereId());
        return versProfil(devoir);
    }

    @Transactional
    public PedagogieDto.DevoirRenduProfil rendre(Long etablissementId, Long eleveId, PedagogieDto.DevoirRenduCreation requete) {
        eleveService.trouver(etablissementId, eleveId);
        Devoir devoir = trouver(requete.devoirId());
        classeMatiereService.obtenir(etablissementId, devoir.getClasseMatiereId());

        DevoirRendu rendu = renduRepository.findByDevoirIdAndEleveId(devoir.getId(), eleveId)
                .orElseGet(() -> {
                    DevoirRendu nouveau = new DevoirRendu();
                    nouveau.setDevoirId(devoir.getId());
                    nouveau.setEleveId(eleveId);
                    return nouveau;
                });
        rendu.setFichierReponseUrl(requete.fichierReponseUrl());
        rendu.setDateSoumission(Instant.now());
        rendu.setStatut(LocalDate.now().isAfter(devoir.getDateLimite()) ? "en_retard" : "rendu");
        renduRepository.save(rendu);
        return versProfilRendu(rendu);
    }

    @Transactional
    public PedagogieDto.DevoirRenduProfil corriger(Long etablissementId, Long utilisateurId, Long renduId,
                                                     PedagogieDto.DevoirCorrection requete) {
        DevoirRendu rendu = renduRepository.findById(renduId)
                .orElseThrow(() -> new RessourceIntrouvableException("DevoirRendu", renduId));
        Devoir devoir = trouver(rendu.getDevoirId());
        classeMatiereService.obtenir(etablissementId, devoir.getClasseMatiereId());
        eleveService.trouver(etablissementId, rendu.getEleveId());

        rendu.setNote(requete.note());
        rendu.setCorrectionCommentaire(requete.correctionCommentaire());
        rendu.setCorrigeParId(utilisateurId);
        rendu.setCorrigeLe(Instant.now());
        rendu.setStatut("corrige");
        renduRepository.save(rendu);
        return versProfilRendu(rendu);
    }

    public List<PedagogieDto.DevoirRenduProfil> rendus(Long etablissementId, Long devoirId) {
        Devoir devoir = trouver(devoirId);
        classeMatiereService.obtenir(etablissementId, devoir.getClasseMatiereId());
        return renduRepository.findByDevoirIdOrderByDateSoumissionDesc(devoirId).stream()
                .map(this::versProfilRendu).toList();
    }

    public List<PedagogieDto.DevoirRenduProfil> mesRendus(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return renduRepository.findByEleveIdOrderByDateSoumissionDesc(eleveId).stream()
                .map(this::versProfilRendu).toList();
    }

    private Devoir trouver(Long id) {
        return devoirRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Devoir", id));
    }

    private PedagogieDto.DevoirProfil versProfil(Devoir d) {
        return new PedagogieDto.DevoirProfil(d.getId(), d.getClasseMatiereId(), d.getTitre(), d.getDescription(),
                d.getDatePublication(), d.getDateLimite(), d.getFichierSujetUrl(), d.getCreatedAt());
    }

    private PedagogieDto.DevoirRenduProfil versProfilRendu(DevoirRendu r) {
        return new PedagogieDto.DevoirRenduProfil(r.getId(), r.getDevoirId(), r.getEleveId(),
                r.getFichierReponseUrl(), r.getDateSoumission(), r.getNote(),
                r.getCorrectionCommentaire(), r.getStatut());
    }
}
