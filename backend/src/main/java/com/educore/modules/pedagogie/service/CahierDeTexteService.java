package com.educore.modules.pedagogie.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.pedagogie.dto.PedagogieDto;
import com.educore.modules.pedagogie.entity.CahierDeTexte;
import com.educore.modules.pedagogie.repository.CahierDeTexteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module 10 — Cahier de texte numerique. */
@Service
public class CahierDeTexteService {

    private final CahierDeTexteRepository cahierRepository;
    private final ClasseMatiereService classeMatiereService;

    public CahierDeTexteService(CahierDeTexteRepository cahierRepository, ClasseMatiereService classeMatiereService) {
        this.cahierRepository = cahierRepository;
        this.classeMatiereService = classeMatiereService;
    }

    @Transactional
    public PedagogieDto.CahierProfil creer(Long etablissementId, Long utilisateurId, PedagogieDto.CahierCreation requete) {
        classeMatiereService.obtenir(etablissementId, requete.classeMatiereId());
        CahierDeTexte entree = new CahierDeTexte();
        entree.setClasseMatiereId(requete.classeMatiereId());
        entree.setDateCours(requete.dateCours());
        entree.setChapitre(requete.chapitre());
        entree.setLecon(requete.lecon());
        entree.setContenu(requete.contenu());
        entree.setExercices(requete.exercices());
        entree.setCreatedById(utilisateurId);
        cahierRepository.save(entree);
        return versProfil(entree);
    }

    public List<PedagogieDto.CahierProfil> lister(Long etablissementId, Long classeMatiereId) {
        classeMatiereService.obtenir(etablissementId, classeMatiereId);
        return cahierRepository.findByClasseMatiereIdOrderByDateCoursDesc(classeMatiereId).stream()
                .map(this::versProfil).toList();
    }

    public PedagogieDto.CahierProfil obtenir(Long etablissementId, Long id) {
        CahierDeTexte entree = cahierRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("CahierDeTexte", id));
        classeMatiereService.obtenir(etablissementId, entree.getClasseMatiereId());
        return versProfil(entree);
    }

    private PedagogieDto.CahierProfil versProfil(CahierDeTexte c) {
        return new PedagogieDto.CahierProfil(c.getId(), c.getClasseMatiereId(), c.getDateCours(),
                c.getChapitre(), c.getLecon(), c.getContenu(), c.getExercices(), c.getCreatedAt());
    }
}
