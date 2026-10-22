package com.educore.modules.paiement.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.paiement.dto.StructureFraisDto;
import com.educore.modules.paiement.entity.StructureFrais;
import com.educore.modules.paiement.repository.StructureFraisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StructureFraisService {
    private final StructureFraisRepository structureFraisRepository;

    public StructureFraisService(StructureFraisRepository structureFraisRepository) {
        this.structureFraisRepository = structureFraisRepository;
    }

    @Transactional
    public StructureFraisDto.Profil creer(Long etablissementId, Long anneeScolaireId, StructureFraisDto.Creation requete) {
        StructureFrais structure = new StructureFrais();
        structure.setEtablissementId(etablissementId);
        structure.setAnneeScolaireId(anneeScolaireId);
        structure.setNiveauId(requete.niveauId());
        structure.setLibelle(requete.libelle());
        structure.setMontant(requete.montant());
        structure.setObligatoire(requete.obligatoire());
        structureFraisRepository.save(structure);
        return versProfil(structure);
    }

    StructureFrais trouver(Long etablissementId, Long structureFraisId) {
        StructureFrais structure = structureFraisRepository.findById(structureFraisId)
                .orElseThrow(() -> new RessourceIntrouvableException("StructureFrais", structureFraisId));
        if (!structure.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("StructureFrais", structureFraisId);
        }
        return structure;
    }

    private StructureFraisDto.Profil versProfil(StructureFrais s) {
        return new StructureFraisDto.Profil(s.getId(), s.getNiveauId(), s.getLibelle(), s.getMontant(), s.isObligatoire());
    }
}
