package com.educore.modules.enseignant.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.common.util.MotDePasseUtils;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.service.AuthService;
import com.educore.modules.auth.service.UtilisateurRoleService;
import com.educore.modules.enseignant.dto.EnseignantDto;
import com.educore.modules.enseignant.entity.Enseignant;
import com.educore.modules.enseignant.repository.EnseignantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creation d'un enseignant = creation d'un compte Utilisateur (role PROFESSEUR,
 * mot de passe temporaire, meme pattern que UtilisateurAdminService/EtablissementService)
 * + son profil professionnel specifique (specialite, contrat, salaire...).
 */
@Service
public class EnseignantService {

    private final EnseignantRepository enseignantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public EnseignantService(EnseignantRepository enseignantRepository, UtilisateurRepository utilisateurRepository,
                              UtilisateurRoleService utilisateurRoleService, AuthService authService, PasswordEncoder passwordEncoder) {
        this.enseignantRepository = enseignantRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EnseignantDto.CreationReponse creer(Long etablissementId, EnseignantDto.Creation requete) {
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
        utilisateur.setMatricule(requete.matricule());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseTemporaire));
        utilisateur.setDoitChangerMdp(true);
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        utilisateurRoleService.attribuerRoles(utilisateur.getId(), List.of("PROFESSEUR"));

        Enseignant enseignant = new Enseignant();
        enseignant.setEtablissementId(etablissementId);
        enseignant.setUtilisateurId(utilisateur.getId());
        enseignant.setMatricule(requete.matricule());
        enseignant.setSpecialite(requete.specialite());
        enseignant.setDiplomePrincipal(requete.diplomePrincipal());
        enseignant.setDateEmbauche(requete.dateEmbauche());
        enseignant.setTypeContrat(requete.typeContrat());
        enseignant.setSalaireBase(requete.salaireBase());
        enseignant.setTauxHoraireSupp(requete.tauxHoraireSupp());
        enseignantRepository.save(enseignant);

        return new EnseignantDto.CreationReponse(versProfil(enseignant), motDePasseTemporaire);
    }

    public List<EnseignantDto.Profil> lister(Long etablissementId) {
        return enseignantRepository.findByEtablissementId(etablissementId).stream().map(this::versProfil).toList();
    }

    /** Utilise pour les controles fins de permission (ex: seul l'enseignant assigne peut saisir ses notes). Null si l'utilisateur n'est pas enseignant. */
    public Long obtenirIdParUtilisateurId(Long utilisateurId) {
        return enseignantRepository.findByUtilisateurId(utilisateurId).map(Enseignant::getId).orElse(null);
    }

    public EnseignantDto.Profil obtenir(Long etablissementId, Long enseignantId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RessourceIntrouvableException("Enseignant", enseignantId));
        if (!enseignant.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Enseignant", enseignantId);
        }
        return versProfil(enseignant);
    }

    private EnseignantDto.Profil versProfil(Enseignant e) {
        var profilUtilisateur = authService.obtenirProfil(e.getUtilisateurId());
        return new EnseignantDto.Profil(e.getId(), profilUtilisateur, e.getMatricule(), e.getSpecialite(),
                e.getDiplomePrincipal(), e.getDateEmbauche(), e.getTypeContrat(), e.getSalaireBase(),
                e.getTauxHoraireSupp(), e.getStatut());
    }
}
