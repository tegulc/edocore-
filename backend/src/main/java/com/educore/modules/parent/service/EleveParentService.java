package com.educore.modules.parent.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.auth.service.AuthService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.parent.dto.EleveParentDto;
import com.educore.modules.parent.entity.EleveParent;
import com.educore.modules.parent.entity.Parent;
import com.educore.modules.parent.repository.EleveParentRepository;
import com.educore.modules.parent.repository.ParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lie un eleve a un parent deja existant (lien de parente, contact principal,
 * autorisation de recuperation). Un seul contact principal actif n'est pas
 * impose en base (plusieurs parents peuvent legitimement se partager ce role),
 * laisse a l'appreciation de l'etablissement.
 */
@Service
public class EleveParentService {

    private final EleveParentRepository eleveParentRepository;
    private final ParentRepository parentRepository;
    private final EleveService eleveService;
    private final ParentService parentService;
    private final AuthService authService;

    public EleveParentService(EleveParentRepository eleveParentRepository, ParentRepository parentRepository,
                               EleveService eleveService, ParentService parentService, AuthService authService) {
        this.eleveParentRepository = eleveParentRepository;
        this.parentRepository = parentRepository;
        this.eleveService = eleveService;
        this.parentService = parentService;
        this.authService = authService;
    }

    @Transactional
    public EleveParentDto.Profil lier(Long etablissementId, Long eleveId, EleveParentDto.Liaison requete) {
        eleveService.trouver(etablissementId, eleveId);
        Parent parent = parentService.trouver(etablissementId, requete.parentId());

        EleveParent lien = eleveParentRepository.findByEleveIdAndParentId(eleveId, parent.getId())
                .orElseGet(() -> {
                    EleveParent nouveau = new EleveParent();
                    nouveau.setEleveId(eleveId);
                    nouveau.setParentId(parent.getId());
                    return nouveau;
                });
        lien.setLienParente(requete.lienParente());
        lien.setEstContactPrincipal(requete.estContactPrincipal());
        lien.setAutoriseRecupererEleve(requete.autoriseRecupererEleve());
        eleveParentRepository.save(lien);

        return versProfil(lien, parent);
    }

    public List<EleveParentDto.Profil> listerParents(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return eleveParentRepository.findByEleveId(eleveId).stream()
                .map(lien -> versProfil(lien, parentRepository.findById(lien.getParentId())
                        .orElseThrow(() -> new RessourceIntrouvableException("Parent", lien.getParentId()))))
                .toList();
    }

    private EleveParentDto.Profil versProfil(EleveParent lien, Parent parent) {
        var profilUtilisateur = authService.obtenirProfil(parent.getUtilisateurId());
        return new EleveParentDto.Profil(lien.getId(), lien.getEleveId(), lien.getParentId(),
                profilUtilisateur.nom(), profilUtilisateur.prenom(), profilUtilisateur.telephone(),
                lien.getLienParente(), lien.isEstContactPrincipal(), lien.isAutoriseRecupererEleve());
    }
}
