package com.educore.modules.auth.service;

import com.educore.common.audit.JournalAuditService;
import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.auth.dto.GestionUtilisateurDto;
import com.educore.modules.auth.dto.UtilisateurDto;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educore.common.util.MotDePasseUtils;
import java.util.Set;

/**
 * Gestion administrative des comptes utilisateurs (creation, modification,
 * changement de statut, gestion des roles). Reservee aux roles ADMIN et
 * DIRECTEUR (verifie au niveau controller via @PreAuthorize, et ici par
 * defense en profondeur).
 *
 * Isolation multi-tenant : un administrateur ne peut jamais lister, creer ou
 * modifier un utilisateur en dehors de son propre etablissement, conformement
 * au principe P1 du document d'architecture.
 *
 * Chaque action sensible est journalisee dans JournalAudit (cahier des
 * charges Module 1 : "historique des connexions et des actions").
 */
@Service
public class UtilisateurAdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final PasswordEncoder passwordEncoder;
    private final JournalAuditService journalAuditService;

    public UtilisateurAdminService(UtilisateurRepository utilisateurRepository, UtilisateurRoleService utilisateurRoleService,
                                    PasswordEncoder passwordEncoder, JournalAuditService journalAuditService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.passwordEncoder = passwordEncoder;
        this.journalAuditService = journalAuditService;
    }

    @Transactional
    public GestionUtilisateurDto.CreationReponse creer(Long etablissementId, Long acteurId, String adresseIp,
                                                         GestionUtilisateurDto.Creation requete) {
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
        utilisateur.setDoitChangerMdp(true); // oblige un changement au premier login
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);

        utilisateurRepository.save(utilisateur);
        utilisateurRoleService.attribuerRoles(utilisateur.getId(), requete.roleCodes());

        journalAuditService.enregistrer(etablissementId, acteurId, "CREATE", "utilisateurs", utilisateur.getId(),
                null, requete, adresseIp);

        return new GestionUtilisateurDto.CreationReponse(versProfil(utilisateur), motDePasseTemporaire);
    }

    @Transactional
    public UtilisateurDto.Profil modifier(Long etablissementId, Long acteurId, String adresseIp,
                                           Long utilisateurId, GestionUtilisateurDto.Modification requete) {
        Utilisateur utilisateur = trouverDansEtablissement(etablissementId, utilisateurId);
        Set<String> rolesAvant = utilisateurRoleService.listerCodesRoles(utilisateurId);

        utilisateur.setNom(requete.nom());
        utilisateur.setPrenom(requete.prenom());
        utilisateur.setTelephone(requete.telephone());

        // Remplacement complet des roles : simple et sans ambiguite pour cette premiere version
        // (une gestion additive fine existe deja via ajouterRole/retirerRole si un besoin plus cible se presente).
        utilisateurRoleService.remplacerRoles(utilisateurId, requete.roleCodes());

        journalAuditService.enregistrer(etablissementId, acteurId, "UPDATE", "utilisateurs", utilisateurId,
                rolesAvant, requete, adresseIp);

        return versProfil(utilisateur);
    }

    @Transactional
    public void changerStatut(Long etablissementId, Long acteurId, String adresseIp, Long utilisateurId, String statut) {
        Utilisateur utilisateur = trouverDansEtablissement(etablissementId, utilisateurId);
        Utilisateur.StatutUtilisateur ancienStatut = utilisateur.getStatut();
        try {
            utilisateur.setStatut(Utilisateur.StatutUtilisateur.valueOf(statut));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("STATUT_INVALIDE", "Statut inconnu : " + statut, HttpStatus.BAD_REQUEST);
        }

        journalAuditService.enregistrer(etablissementId, acteurId, "UPDATE", "utilisateurs", utilisateurId,
                java.util.Map.of("statut", ancienStatut), java.util.Map.of("statut", statut), adresseIp);
    }

    @Transactional
    public void ajouterRole(Long etablissementId, Long acteurId, String adresseIp, Long utilisateurId, String roleCode) {
        trouverDansEtablissement(etablissementId, utilisateurId);
        utilisateurRoleService.ajouterRole(utilisateurId, roleCode);
        journalAuditService.enregistrer(etablissementId, acteurId, "UPDATE", "utilisateur_roles", utilisateurId,
                null, java.util.Map.of("roleAjoute", roleCode), adresseIp);
    }

    @Transactional
    public void retirerRole(Long etablissementId, Long acteurId, String adresseIp, Long utilisateurId, String roleCode) {
        trouverDansEtablissement(etablissementId, utilisateurId);
        utilisateurRoleService.retirerRole(utilisateurId, roleCode);
        journalAuditService.enregistrer(etablissementId, acteurId, "UPDATE", "utilisateur_roles", utilisateurId,
                java.util.Map.of("roleRetire", roleCode), null, adresseIp);
    }

    public Page<UtilisateurDto.Profil> lister(Long etablissementId, Pageable pageable) {
        return utilisateurRepository.findByEtablissementId(etablissementId, pageable).map(this::versProfil);
    }

    private Utilisateur trouverDansEtablissement(Long etablissementId, Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));
        if (!utilisateur.getEtablissementId().equals(etablissementId)) {
            // Volontairement la meme exception que "introuvable" : ne jamais reveler l'existence
            // d'un utilisateur d'un autre etablissement (principe P1 - isolation multi-tenant).
            throw new RessourceIntrouvableException("Utilisateur", utilisateurId);
        }
        return utilisateur;
    }

    private UtilisateurDto.Profil versProfil(Utilisateur u) {
        var rolePrincipal = utilisateurRoleService.obtenirRolePrincipal(u.getId());
        var roles = utilisateurRoleService.listerCodesRoles(u.getId());
        return new UtilisateurDto.Profil(
                u.getId(), u.getEtablissementId(), rolePrincipal.getCode(), roles, u.getMatricule(),
                u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(), u.getPhotoUrl(),
                u.getLanguePreferee(), u.isDeuxFaActive(), u.isDoitChangerMdp());
    }
}
