package com.educore.modules.classe.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.classe.dto.MatiereDto;
import com.educore.modules.classe.entity.Matiere;
import com.educore.modules.classe.repository.MatiereRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatiereService {
    private final MatiereRepository matiereRepository;

    public MatiereService(MatiereRepository matiereRepository) { this.matiereRepository = matiereRepository; }

    @Transactional
    public MatiereDto.Profil creer(Long etablissementId, MatiereDto.Creation requete) {
        if (matiereRepository.existsByEtablissementIdAndCode(etablissementId, requete.code())) {
            throw new BusinessException("MATIERE_DEJA_EXISTANTE", "Cette matiere existe deja.", HttpStatus.CONFLICT);
        }
        Matiere matiere = new Matiere();
        matiere.setEtablissementId(etablissementId);
        matiere.setCode(requete.code());
        matiere.setLibelle(requete.libelle());
        if (requete.couleur() != null) matiere.setCouleur(requete.couleur());
        matiereRepository.save(matiere);
        return versProfil(matiere);
    }

    public List<MatiereDto.Profil> lister(Long etablissementId) {
        return matiereRepository.findByEtablissementIdOrderByLibelleAsc(etablissementId).stream().map(this::versProfil).toList();
    }

    public MatiereDto.Profil obtenir(Long etablissementId, Long matiereId) {
        Matiere matiere = matiereRepository.findById(matiereId)
                .filter(m -> m.getEtablissementId().equals(etablissementId))
                .orElseThrow(() -> new com.educore.common.exception.RessourceIntrouvableException("Matiere", matiereId));
        return versProfil(matiere);
    }

    private MatiereDto.Profil versProfil(Matiere m) {
        return new MatiereDto.Profil(m.getId(), m.getCode(), m.getLibelle(), m.getCouleur(),
                m.getTypeMatiere(), m.getLangueVivanteNiveau(), m.getIsOption(), m.getDescription());
    }
}
