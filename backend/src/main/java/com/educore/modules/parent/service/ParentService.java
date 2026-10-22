package com.educore.modules.parent.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.common.util.MotDePasseUtils;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.service.AuthService;
import com.educore.modules.auth.service.UtilisateurRoleService;
import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.parent.dto.ParentDto;
import com.educore.modules.parent.entity.Parent;
import com.educore.modules.parent.repository.EleveParentRepository;
import com.educore.modules.parent.repository.ParentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Meme pattern que EnseignantService : compte Utilisateur (role PARENT) + profil specifique, crees ensemble. */
@Service
public class ParentService {

    private final ParentRepository parentRepository;
    private final EleveParentRepository eleveParentRepository;
    private final EleveService eleveService;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public ParentService(ParentRepository parentRepository, EleveParentRepository eleveParentRepository,
                          EleveService eleveService, UtilisateurRepository utilisateurRepository,
                          UtilisateurRoleService utilisateurRoleService, AuthService authService,
                          PasswordEncoder passwordEncoder) {
        this.parentRepository = parentRepository;
        this.eleveParentRepository = eleveParentRepository;
        this.eleveService = eleveService;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ParentDto.CreationReponse creer(Long etablissementId, ParentDto.Creation requete) {
        utilisateurRepository.findByEmailAndEtablissementId(requete.email(), etablissementId).ifPresent(u -> {
            throw new BusinessException("EMAIL_DEJA_UTILISE", "Un compte existe deja avec cet email dans cet etablissement.", HttpStatus.CONFLICT);
        });

        String motDePasseTemporaire = MotDePasseUtils.genererMotDePasseTemporaire();

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEtablissementId(etablissementId);
        utilisateur.setNom(requete.nom());
        utilisateur.setPrenom(requete.prenom());
        utilisateur.setEmail(requete.email());
        utilisateur.setTelephone(requete.telephone());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseTemporaire));
        utilisateur.setDoitChangerMdp(true);
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        utilisateurRoleService.attribuerRoles(utilisateur.getId(), List.of("PARENT"));

        Parent parent = new Parent();
        parent.setEtablissementId(etablissementId);
        parent.setUtilisateurId(utilisateur.getId());
        parent.setProfession(requete.profession());
        parent.setAdresse(requete.adresse());
        parentRepository.save(parent);

        return new ParentDto.CreationReponse(versProfil(parent), motDePasseTemporaire);
    }

    public List<ParentDto.Profil> lister(Long etablissementId) {
        return parentRepository.findByEtablissementId(etablissementId).stream().map(this::versProfil).toList();
    }

    public List<EleveDto.Profil> listerMesEnfants(Long etablissementId, Long utilisateurId) {
        Parent parent = parentRepository.findByUtilisateurId(utilisateurId)
                .filter(p -> p.getEtablissementId().equals(etablissementId))
                .orElseThrow(() -> new RessourceIntrouvableException("Parent", utilisateurId));
        return eleveParentRepository.findByParentId(parent.getId()).stream()
                .map(lien -> eleveService.obtenir(etablissementId, lien.getEleveId()))
                .toList();
    }

    /** Utilise par d'autres modules (ex: Notes/Bulletins) pour notifier le compte utilisateur lie a ce parent. */
    public Long obtenirUtilisateurId(Long etablissementId, Long parentId) {
        return trouver(etablissementId, parentId).getUtilisateurId();
    }

    Parent trouver(Long etablissementId, Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RessourceIntrouvableException("Parent", parentId));
        if (!parent.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Parent", parentId);
        }
        return parent;
    }

    private ParentDto.Profil versProfil(Parent p) {
        return new ParentDto.Profil(p.getId(), authService.obtenirProfil(p.getUtilisateurId()), p.getProfession(), p.getAdresse());
    }
}
