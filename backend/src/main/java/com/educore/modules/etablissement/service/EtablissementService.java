package com.educore.modules.etablissement.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.common.util.MotDePasseUtils;
import com.educore.modules.auth.dto.UtilisateurDto;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.service.UtilisateurRoleService;
import com.educore.modules.etablissement.dto.EtablissementDto;
import com.educore.modules.etablissement.entity.Etablissement;
import com.educore.modules.etablissement.entity.EtablissementModule;
import com.educore.modules.etablissement.repository.EtablissementModuleRepository;
import com.educore.modules.etablissement.repository.EtablissementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestion des etablissements - racine du multi-tenant (voir document
 * d'architecture, Partie 7). inscrire() est le SEUL point d'entree public de
 * toute l'API : il resout le probleme de demarrage (aucun utilisateur ne
 * peut exister avant qu'un etablissement existe) en creant l'etablissement
 * ET son premier compte ADMIN dans la meme transaction.
 */
@Service
public class EtablissementService {

    private final EtablissementRepository etablissementRepository;
    private final EtablissementModuleRepository etablissementModuleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final PasswordEncoder passwordEncoder;
    private final AnneeScolaireService anneeScolaireService;

    public EtablissementService(EtablissementRepository etablissementRepository,
                                 EtablissementModuleRepository etablissementModuleRepository,
                                 UtilisateurRepository utilisateurRepository,
                                 UtilisateurRoleService utilisateurRoleService,
                                 PasswordEncoder passwordEncoder,
                                 AnneeScolaireService anneeScolaireService) {
        this.etablissementRepository = etablissementRepository;
        this.etablissementModuleRepository = etablissementModuleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.passwordEncoder = passwordEncoder;
        this.anneeScolaireService = anneeScolaireService;
    }

    @Transactional
    public EtablissementDto.InscriptionReponse inscrire(EtablissementDto.Inscription requete) {
        if (requete.email() != null && etablissementRepository.existsByEmail(requete.email())) {
            throw new BusinessException("ETABLISSEMENT_DEJA_INSCRIT", "Un etablissement est deja inscrit avec cet email.", HttpStatus.CONFLICT);
        }
        utilisateurRepository.findByEmail(requete.administrateur().email()).ifPresent(u -> {
            throw new BusinessException("EMAIL_DEJA_UTILISE", "Cet email administrateur est deja utilise sur la plateforme.", HttpStatus.CONFLICT);
        });

        Etablissement etablissement = new Etablissement();
        etablissement.setNom(requete.nom());
        etablissement.setSigle(requete.sigle());
        etablissement.setPays(requete.pays());
        etablissement.setVille(requete.ville());
        etablissement.setTypeEtablissement(requete.typeEtablissement());
        etablissement.setTelephone(requete.telephone());
        etablissement.setEmail(requete.email());
        etablissement.setStatut(Etablissement.StatutEtablissement.ACTIF);
        etablissement.setPlanAbonnement("essai");
        etablissement.setDateDebutAbonnement(LocalDate.now());
        etablissementRepository.save(etablissement);

        // Module socle : coeur pedagogique minimal actif des l'inscription, le reste
        // s'active a la demande (cahier des charges 3.1, deploiement "progressif, module par module").
        for (String code : ModuleCatalogue.MODULES_ACTIFS_PAR_DEFAUT) {
            EtablissementModule module = new EtablissementModule();
            module.setEtablissementId(etablissement.getId());
            module.setModuleCode(code);
            module.setActif(true);
            etablissementModuleRepository.save(module);
        }

        String motDePasseTemporaire = MotDePasseUtils.genererMotDePasseTemporaire();

        Utilisateur admin = new Utilisateur();
        admin.setEtablissementId(etablissement.getId());
        admin.setNom(requete.administrateur().nom());
        admin.setPrenom(requete.administrateur().prenom());
        admin.setEmail(requete.administrateur().email());
        admin.setTelephone(requete.administrateur().telephone());
        admin.setMotDePasseHash(passwordEncoder.encode(motDePasseTemporaire));
        admin.setDoitChangerMdp(true);
        admin.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(admin);

        utilisateurRoleService.attribuerRoles(admin.getId(), List.of("ADMIN"));

        // Onboarding sans friction : une premiere annee scolaire active des la creation,
        // pour que l'etablissement soit utilisable immediatement (l'admin peut la modifier ensuite).
        anneeScolaireService.creerAnneeParDefaut(etablissement.getId());

        return new EtablissementDto.InscriptionReponse(versProfil(etablissement), versProfilUtilisateur(admin), motDePasseTemporaire);
    }

    public EtablissementDto.Profil obtenirProfil(Long etablissementId) {
        return versProfil(trouver(etablissementId));
    }

    @Transactional
    public EtablissementDto.Profil modifier(Long etablissementId, EtablissementDto.Modification requete) {
        Etablissement etablissement = trouver(etablissementId);
        etablissement.setNom(requete.nom());
        etablissement.setSigle(requete.sigle());
        etablissement.setLogoUrl(requete.logoUrl());
        etablissement.setVille(requete.ville());
        etablissement.setAdresse(requete.adresse());
        etablissement.setTelephone(requete.telephone());
        etablissement.setEmail(requete.email());
        etablissement.setTypeEtablissement(requete.typeEtablissement());
        if (requete.devise() != null) etablissement.setDevise(requete.devise());
        if (requete.langueDefaut() != null) etablissement.setLangueDefaut(requete.langueDefaut());
        if (requete.fuseauHoraire() != null) etablissement.setFuseauHoraire(requete.fuseauHoraire());
        etablissement.setUpdatedAt(java.time.Instant.now());
        return versProfil(etablissement);
    }

    private Etablissement trouver(Long etablissementId) {
        return etablissementRepository.findById(etablissementId)
                .orElseThrow(() -> new RessourceIntrouvableException("Etablissement", etablissementId));
    }

    private EtablissementDto.Profil versProfil(Etablissement e) {
        return new EtablissementDto.Profil(e.getId(), e.getNom(), e.getSigle(), e.getLogoUrl(), e.getPays(),
                e.getVille(), e.getAdresse(), e.getTelephone(), e.getEmail(), e.getTypeEtablissement(),
                e.getStatut().name(), e.getPlanAbonnement(), e.getDevise(), e.getLangueDefaut(), e.getFuseauHoraire());
    }

    private UtilisateurDto.Profil versProfilUtilisateur(Utilisateur u) {
        return new UtilisateurDto.Profil(u.getId(), u.getEtablissementId(), "ADMIN", java.util.Set.of("ADMIN"),
                u.getMatricule(), u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(), u.getPhotoUrl(),
                u.getLanguePreferee(), u.isDeuxFaActive(), u.isDoitChangerMdp());
    }
}
