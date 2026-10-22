package com.educore.modules.auth.service;

import com.educore.common.audit.JournalAuditService;
import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.auth.dto.ConnexionDto;
import com.educore.modules.auth.dto.DeuxFaDto;
import com.educore.modules.auth.dto.UtilisateurDto;
import com.educore.modules.auth.entity.HistoriqueConnexion;
import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.entity.UtilisateurSession;
import com.educore.modules.auth.repository.HistoriqueConnexionRepository;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.repository.UtilisateurSessionRepository;
import com.educore.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regles metier de l'authentification (Module 1 - Gestion des utilisateurs / Securite).
 *
 * Regle 01 : un compte SUSPENDU ou ARCHIVE ne peut jamais se connecter.
 * Regle 02 : si le 2FA est active, un code TOTP valide est obligatoire en plus du mot de passe.
 * Regle 03 : chaque tentative (succes/echec) est journalisee dans historique_connexions.
 * Regle 04 : le mot de passe n'est jamais renvoye ni journalise en clair.
 * Regle 05 : chaque refresh token emis correspond a une session revocable individuellement.
 * Regle 06 : le refresh token est stocke hache (SHA-256), jamais en clair, meme en base.
 * Regle 07 : un utilisateur peut porter plusieurs roles ; le JWT et le RBAC portent
 *            l'union de tous ses roles, jamais seulement le role principal.
 * Regle 08 : le mot de passe doit respecter une complexite minimale et expire apres
 *            une duree configurable (cahier des charges, section 4.3).
 */
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final UtilisateurSessionRepository sessionRepository;
    private final HistoriqueConnexionRepository historiqueRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final JournalAuditService journalAuditService;

    @Value("${educore.security.mot-de-passe.duree-validite-jours:90}")
    private int dureeValiditeMotDePasseJours;

    public AuthService(UtilisateurRepository utilisateurRepository, UtilisateurRoleService utilisateurRoleService,
                        UtilisateurSessionRepository sessionRepository, HistoriqueConnexionRepository historiqueRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService, TotpService totpService,
                        JournalAuditService journalAuditService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.sessionRepository = sessionRepository;
        this.historiqueRepository = historiqueRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.totpService = totpService;
        this.journalAuditService = journalAuditService;
    }

    @Transactional
    public ConnexionDto.Reponse connecter(ConnexionDto.Requete requete, String adresseIp, String userAgent) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.email()).orElse(null);

        if (utilisateur == null || !passwordEncoder.matches(requete.motDePasse(), utilisateur.getMotDePasseHash())) {
            if (utilisateur != null) {
                journaliserTentative(utilisateur.getId(), adresseIp, userAgent, false);
            }
            throw new BadCredentialsException("Identifiants invalides");
        }

        if (utilisateur.getStatut() == Utilisateur.StatutUtilisateur.SUSPENDU
                || utilisateur.getStatut() == Utilisateur.StatutUtilisateur.ARCHIVE) {
            journaliserTentative(utilisateur.getId(), adresseIp, userAgent, false);
            throw new BusinessException("COMPTE_INDISPONIBLE",
                    "Ce compte est suspendu ou archive. Contactez l'administration de votre etablissement.",
                    HttpStatus.FORBIDDEN);
        }

        // Volume 6 (Documentation API), §4.1 : la connexion ne renvoie jamais une erreur pour le 2FA.
        // Le mot de passe est deja valide a ce stade ; on emet un jeton transitoire tres court plutot
        // que les jetons definitifs, et le frontend acheve la connexion via POST /auth/2fa/verifier.
        if (utilisateur.isDeuxFaActive()) {
            journaliserTentative(utilisateur.getId(), adresseIp, userAgent, true); // mot de passe correct = tentative "reussie" a ce stade
            return ConnexionDto.Reponse.deuxFaRequise(jwtService.genererJetonDeuxFaPending(utilisateur.getId()));
        }

        return finaliserConnexion(utilisateur, adresseIp, userAgent);
    }

    @Transactional
    public UtilisateurDto.Profil inscrire(com.educore.modules.auth.dto.InscriptionDto.Requete requete) {
        // Verifie doublon email dans le meme etablissement
        utilisateurRepository.findByEmailAndEtablissementId(requete.email(), requete.etablissementId()).ifPresent(u -> {
            throw new BusinessException("EMAIL_DEJA_UTILISE", "Un compte existe deja avec cet email dans cet etablissement.", org.springframework.http.HttpStatus.CONFLICT);
        });

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEtablissementId(requete.etablissementId());
        utilisateur.setNom(requete.nom());
        utilisateur.setPrenom(requete.prenom());
        utilisateur.setEmail(requete.email());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(requete.motDePasse()));
        utilisateur.setDoitChangerMdp(false);
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);

        utilisateurRepository.save(utilisateur);

        // Attribue role par defaut ELEVE si aucun role fourni
        try {
            utilisateurRoleService.attribuerRoles(utilisateur.getId(), java.util.List.of("ELEVE"));
        } catch (Exception e) {
            // Nettoyage si l'attribution echoue
            utilisateurRepository.deleteById(utilisateur.getId());
            throw e;
        }

        // Journal audit : l'utilisateur est l'acteur de sa propre creation
        journalAuditService.enregistrer(utilisateur.getEtablissementId(), utilisateur.getId(), "CREATE", "utilisateurs", utilisateur.getId(), null, requete, null);

        com.educore.modules.auth.entity.Role rolePrincipal = utilisateurRoleService.obtenirRolePrincipal(utilisateur.getId());
        java.util.Set<String> tousLesRoles = utilisateurRoleService.listerCodesRoles(utilisateur.getId());

        return versProfil(utilisateur, rolePrincipal.getCode(), tousLesRoles);
    }

    /**
     * Etape 2 quand le 2FA est actif : verifie le code TOTP contre le jeton transitoire
     * emis par connecter(), puis emet les jetons definitifs (POST /auth/2fa/verifier).
     */
    @Transactional
    public ConnexionDto.Reponse verifierDeuxFaEtConnecter(String jetonDeuxFaPending, String code, String adresseIp, String userAgent) {
        if (!jwtService.estValide(jetonDeuxFaPending) || !"deux_fa_pending".equals(jwtService.extraireType(jetonDeuxFaPending))) {
            throw new BusinessException("JETON_DEUX_FA_INVALIDE", "Session de verification 2FA invalide ou expiree. Reconnectez-vous.", HttpStatus.UNAUTHORIZED);
        }
        Long utilisateurId = jwtService.extraireUtilisateurId(jetonDeuxFaPending);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        if (!totpService.verifier(utilisateur.getDeuxFaSecret(), code)) {
            journaliserTentative(utilisateurId, adresseIp, userAgent, false);
            throw new BusinessException("DEUX_FA_INVALIDE", "Code d'authentification incorrect.", HttpStatus.UNAUTHORIZED);
        }

        return finaliserConnexion(utilisateur, adresseIp, userAgent);
    }

    private ConnexionDto.Reponse finaliserConnexion(Utilisateur utilisateur, String adresseIp, String userAgent) {
        // Regle 08 : expiration du mot de passe detectee au login, force le changement sans bloquer la connexion.
        if (utilisateur.getMotDePasseExpireLe() != null && Instant.now().isAfter(utilisateur.getMotDePasseExpireLe())) {
            utilisateur.setDoitChangerMdp(true);
        }

        Role rolePrincipal = utilisateurRoleService.obtenirRolePrincipal(utilisateur.getId());
        Set<String> tousLesRoles = utilisateurRoleService.listerCodesRoles(utilisateur.getId());

        utilisateur.setDerniereConnexion(Instant.now());
        journaliserTentative(utilisateur.getId(), adresseIp, userAgent, true);

        String accessToken = jwtService.genererAccessToken(utilisateur.getId(), utilisateur.getEtablissementId(), rolePrincipal.getCode(), tousLesRoles);
        String refreshToken = jwtService.genererRefreshToken(utilisateur.getId());
        enregistrerSession(utilisateur.getId(), refreshToken, adresseIp, userAgent);

        return ConnexionDto.Reponse.succes(accessToken, refreshToken, jwtService.accessExpirationSecondes(),
                versProfil(utilisateur, rolePrincipal.getCode(), tousLesRoles));
    }

    /**
     * Rafraichit un access token expire a partir d'un refresh token valide.
     * Rotation systematique : l'ancien refresh token est revoque, un nouveau est emis
     * (limite la fenetre d'exploitation en cas de vol du jeton).
     */
    @Transactional
    public ConnexionDto.Reponse rafraichir(String refreshToken) {
        if (!jwtService.estValide(refreshToken)) {
            throw new BusinessException("JETON_INVALIDE", "Le jeton de rafraichissement est invalide ou expire.", HttpStatus.UNAUTHORIZED);
        }
        String hash = hacher(refreshToken);
        UtilisateurSession session = sessionRepository.findByTokenHashAndRevoqueFalse(hash)
                .orElseThrow(() -> new BusinessException("SESSION_REVOQUEE", "Cette session a ete revoquee. Reconnectez-vous.", HttpStatus.UNAUTHORIZED));

        Long utilisateurId = jwtService.extraireUtilisateurId(refreshToken);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        Role rolePrincipal = utilisateurRoleService.obtenirRolePrincipal(utilisateurId);
        Set<String> tousLesRoles = utilisateurRoleService.listerCodesRoles(utilisateurId);

        session.setRevoque(true); // rotation : l'ancien refresh token ne peut plus etre reutilise

        String nouvelAccessToken = jwtService.genererAccessToken(utilisateur.getId(), utilisateur.getEtablissementId(), rolePrincipal.getCode(), tousLesRoles);
        String nouveauRefreshToken = jwtService.genererRefreshToken(utilisateur.getId());
        enregistrerSession(utilisateur.getId(), nouveauRefreshToken, session.getAdresseIp(), session.getUserAgent());

        return ConnexionDto.Reponse.succes(nouvelAccessToken, nouveauRefreshToken, jwtService.accessExpirationSecondes(),
                versProfil(utilisateur, rolePrincipal.getCode(), tousLesRoles));
    }

    /** Revoque la session correspondant au refresh token fourni (deconnexion de l'appareil courant uniquement). */
    @Transactional
    public void deconnecter(String refreshToken) {
        sessionRepository.revoquerParTokenHash(hacher(refreshToken));
    }

    /** Revoque toutes les sessions actives d'un utilisateur (ex : "deconnecter tous les appareils", ou suite a compromission). */
    @Transactional
    public void deconnecterTousLesAppareils(Long utilisateurId) {
        sessionRepository.revoquerToutesLesSessions(utilisateurId);
    }

    @Transactional
    public ConnexionDto.Reponse changerMotDePasse(Long utilisateurId, String ancienMotDePasse, String nouveauMotDePasse,
                                                   String adresseIp, String userAgent) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasseHash())) {
            throw new BusinessException("MOT_DE_PASSE_ACTUEL_INCORRECT", "Le mot de passe actuel est incorrect.", HttpStatus.BAD_REQUEST);
        }
        validerComplexite(nouveauMotDePasse);

        appliquerNouveauMotDePasse(utilisateur, nouveauMotDePasse);
        utilisateurRepository.save(utilisateur);
        deconnecterTousLesAppareils(utilisateurId);
        // Nouvelle session pour l'appareil courant : l'utilisateur reste connecte avec le nouveau mot de passe.
        return finaliserConnexion(utilisateur, adresseIp, userAgent);
    }

    /** Applique un nouveau hash + reinitialise l'horodatage d'expiration selon la politique configuree (regle 08). */
    private void appliquerNouveauMotDePasse(Utilisateur utilisateur, String nouveauMotDePasseClair) {
        utilisateur.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasseClair));
        utilisateur.setDoitChangerMdp(false);
        utilisateur.setMotDePasseExpireLe(dureeValiditeMotDePasseJours > 0
                ? Instant.now().plus(dureeValiditeMotDePasseJours, ChronoUnit.DAYS)
                : null);
    }

    /**
     * Politique de complexite minimale (cahier des charges 4.3) : au moins 8 caracteres,
     * une majuscule, une minuscule et un chiffre.
     */
    private void validerComplexite(String motDePasse) {
        if (motDePasse == null || !motDePasse.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new BusinessException("MOT_DE_PASSE_TROP_FAIBLE",
                    "Le mot de passe doit contenir au moins 8 caracteres, une majuscule, une minuscule et un chiffre.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Demande de reinitialisation de mot de passe. Reponse volontairement identique que le compte
     * existe ou non (evite d'exposer par ce biais quels emails sont enregistres dans la plateforme).
     */
    @Transactional
    public void demanderReinitialisationMotDePasse(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(u -> {
            // TODO Phase 2 : generer un jeton a usage unique + envoyer l'email via le module Notifications.
        });
    }

    public UtilisateurDto.Profil obtenirProfil(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));
        Role rolePrincipal = utilisateurRoleService.obtenirRolePrincipal(utilisateurId);
        Set<String> tousLesRoles = utilisateurRoleService.listerCodesRoles(utilisateurId);
        return versProfil(utilisateur, rolePrincipal.getCode(), tousLesRoles);
    }

    /**
     * Etape 1 de l'activation du 2FA : genere un secret et l'URI otpauth:// a afficher en QR code.
     * Le secret est deja persiste sur l'utilisateur mais deuxFaActive reste false tant que
     * confirmerActivationDeuxFa n'a pas verifie un premier code valide (evite de verrouiller un
     * compte si l'utilisateur scanne mal le QR code).
     */
    @Transactional
    public DeuxFaDto.Provisionnement initierActivationDeuxFa(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        String secret = totpService.genererNouveauSecret();
        utilisateur.setDeuxFaSecret(secret);
        utilisateur.setDeuxFaActive(false);

        String uri = totpService.construireUriProvisionnement(secret, utilisateur.getEmail());
        return new DeuxFaDto.Provisionnement(secret, uri);
    }

    /** Etape 2 : confirme l'activation avec un premier code TOTP valide. */
    @Transactional
    public void confirmerActivationDeuxFa(Long utilisateurId, String code) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        if (utilisateur.getDeuxFaSecret() == null) {
            throw new BusinessException("DEUX_FA_NON_INITIE", "Aucune activation 2FA en cours pour ce compte.", HttpStatus.BAD_REQUEST);
        }
        if (!totpService.verifier(utilisateur.getDeuxFaSecret(), code)) {
            throw new BusinessException("DEUX_FA_INVALIDE", "Code d'authentification incorrect.", HttpStatus.UNAUTHORIZED);
        }
        utilisateur.setDeuxFaActive(true);
    }

    /** Desactive le 2FA. Exige le mot de passe pour eviter qu'un jeton vole seul suffise a l'affaiblir. */
    @Transactional
    public void desactiverDeuxFa(Long utilisateurId, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new BusinessException("MOT_DE_PASSE_ACTUEL_INCORRECT", "Mot de passe incorrect.", HttpStatus.BAD_REQUEST);
        }
        utilisateur.setDeuxFaActive(false);
        utilisateur.setDeuxFaSecret(null);
    }

    private void journaliserTentative(Long utilisateurId, String adresseIp, String userAgent, boolean succes) {
        HistoriqueConnexion entree = new HistoriqueConnexion();
        entree.setUtilisateurId(utilisateurId);
        entree.setAdresseIp(adresseIp);
        entree.setUserAgent(userAgent);
        entree.setSucces(succes);
        historiqueRepository.save(entree);
    }

    @Value("${educore.security.jwt.refresh-expiration-seconds}")
    private long refreshExpirationSecondes;

    private void enregistrerSession(Long utilisateurId, String refreshToken, String adresseIp, String userAgent) {
        UtilisateurSession session = new UtilisateurSession();
        session.setUtilisateurId(utilisateurId);
        session.setTokenHash(hacher(refreshToken));
        session.setAdresseIp(adresseIp);
        session.setUserAgent(userAgent);
        session.setDateExpiration(Instant.now().plusSeconds(refreshExpirationSecondes));
        sessionRepository.save(session);
    }

    private String hacher(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(valeur.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors du hachage du jeton", e);
        }
    }

    private UtilisateurDto.Profil versProfil(Utilisateur u, String rolePrincipal, Set<String> tousLesRoles) {
        return new UtilisateurDto.Profil(
                u.getId(), u.getEtablissementId(), rolePrincipal, tousLesRoles, u.getMatricule(),
                u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(), u.getPhotoUrl(),
                u.getLanguePreferee(), u.isDeuxFaActive(), u.isDoitChangerMdp());
    }
}
