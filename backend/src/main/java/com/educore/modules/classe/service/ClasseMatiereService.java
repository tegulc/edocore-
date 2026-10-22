package com.educore.modules.classe.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.dto.ClasseMatiereDto;
import com.educore.modules.classe.entity.Classe;
import com.educore.modules.classe.entity.ClasseMatiere;
import com.educore.modules.classe.entity.Matiere;
import com.educore.modules.classe.repository.ClasseMatiereRepository;
import com.educore.modules.classe.repository.MatiereRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Affecte une matiere (et eventuellement un enseignant) a une classe, avec coefficient et volume horaire. */
@Service
public class ClasseMatiereService {

    private final ClasseMatiereRepository classeMatiereRepository;
    private final MatiereRepository matiereRepository;
    private final ClasseService classeService;

    public ClasseMatiereService(ClasseMatiereRepository classeMatiereRepository, MatiereRepository matiereRepository,
                                 ClasseService classeService) {
        this.classeMatiereRepository = classeMatiereRepository;
        this.matiereRepository = matiereRepository;
        this.classeService = classeService;
    }

    @Transactional
    public ClasseMatiereDto.Profil affecter(Long etablissementId, Long classeId, ClasseMatiereDto.Affectation requete) {
        Classe classe = classeService.trouver(etablissementId, classeId);
        Matiere matiere = matiereRepository.findById(requete.matiereId())
                .filter(m -> m.getEtablissementId().equals(etablissementId))
                .orElseThrow(() -> new RessourceIntrouvableException("Matiere", requete.matiereId()));

        ClasseMatiere affectation = classeMatiereRepository.findByClasseIdAndMatiereId(classe.getId(), matiere.getId())
                .orElseGet(() -> {
                    ClasseMatiere nouvelle = new ClasseMatiere();
                    nouvelle.setClasseId(classe.getId());
                    nouvelle.setMatiereId(matiere.getId());
                    return nouvelle;
                });
        affectation.setEnseignantId(requete.enseignantId());
        affectation.setCoefficient(requete.coefficient());
        affectation.setVolumeHoraireHebdo(requete.volumeHoraireHebdo());
        classeMatiereRepository.save(affectation);

        return versProfil(affectation, matiere.getLibelle());
    }

    public List<ClasseMatiereDto.Profil> lister(Long etablissementId, Long classeId) {
        classeService.trouver(etablissementId, classeId);
        return classeMatiereRepository.findByClasseId(classeId).stream()
                .map(cm -> versProfil(cm, matiereRepository.findById(cm.getMatiereId()).map(Matiere::getLibelle).orElse("?")))
                .toList();
    }

    /** Point d'entree public pour les autres modules (ex: Evaluations/Notes) - verifie le tenant via la classe parente. */
    public ClasseMatiereDto.Profil obtenir(Long etablissementId, Long classeMatiereId) {
        ClasseMatiere cm = classeMatiereRepository.findById(classeMatiereId)
                .orElseThrow(() -> new RessourceIntrouvableException("ClasseMatiere", classeMatiereId));
        classeService.trouver(etablissementId, cm.getClasseId()); // leve RessourceIntrouvableException si mauvais tenant
        String libelle = matiereRepository.findById(cm.getMatiereId()).map(Matiere::getLibelle).orElse("?");
        return versProfil(cm, libelle);
    }

    private ClasseMatiereDto.Profil versProfil(ClasseMatiere cm, String matiereLibelle) {
        return new ClasseMatiereDto.Profil(cm.getId(), cm.getClasseId(), cm.getMatiereId(), matiereLibelle,
                cm.getEnseignantId(), cm.getCoefficient(), cm.getVolumeHoraireHebdo());
    }
}
