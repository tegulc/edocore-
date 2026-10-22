package com.educore.modules.auth.controller;

import com.educore.common.util.RequeteUtils;
import com.educore.modules.auth.dto.ChangerMotDePasseDto;
import com.educore.modules.auth.dto.ConnexionDto;
import com.educore.modules.auth.dto.DeuxFaDto;
import com.educore.modules.auth.dto.MotDePasseOublieDto;
import com.educore.modules.auth.dto.RafraichirDto;
import com.educore.modules.auth.dto.UtilisateurDto;
import com.educore.modules.auth.service.AuthService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d'authentification (Module 1). Noms conformes au Volume 6
 * (Documentation API) : verbes techniques en anglais pour les actions
 * d'authentification standard (login, refresh), le reste du domaine metier
 * de l'API restant en francais (/utilisateurs, /eleves...).
 *
 * Publics : /login, /refresh, /2fa/verifier, /mot-de-passe-oublie (voir
 * SecurityConfig). Tous les autres exigent un access token valide.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Connexion, rafraichissement de jeton, 2FA, gestion du mot de passe")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion par email/mot de passe. Retourne deux_fa_requis=true + un jeton transitoire si le 2FA est actif.")
    public ResponseEntity<ConnexionDto.Reponse> login(@Valid @RequestBody ConnexionDto.Requete requete, HttpServletRequest requeteHttp) {
        var reponse = authService.connecter(requete, RequeteUtils.extraireIp(requeteHttp), requeteHttp.getHeader("User-Agent"));
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/2fa/verifier")
    @Operation(summary = "Termine la connexion en verifiant le code TOTP, a partir du jeton transitoire retourne par /login")
    public ResponseEntity<ConnexionDto.Reponse> verifierDeuxFa(@Valid @RequestBody DeuxFaDto.Verification requete, HttpServletRequest requeteHttp) {
        var reponse = authService.verifierDeuxFaEtConnecter(requete.jetonDeuxFa(), requete.code(),
                RequeteUtils.extraireIp(requeteHttp), requeteHttp.getHeader("User-Agent"));
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Emet un nouveau access token a partir d'un refresh token valide (avec rotation)")
    public ResponseEntity<ConnexionDto.Reponse> refresh(@Valid @RequestBody RafraichirDto.Requete requete) {
        return ResponseEntity.ok(authService.rafraichir(requete.jetonRafraichissement()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoque la session courante (cet appareil uniquement)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RafraichirDto.Requete requete) {
        authService.deconnecter(requete.jetonRafraichissement());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mot-de-passe-oublie")
    @Operation(summary = "Demande de reinitialisation de mot de passe par email")
    public ResponseEntity<Void> motDePasseOublie(@Valid @RequestBody MotDePasseOublieDto.Requete requete) {
        authService.demanderReinitialisationMotDePasse(requete.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription publique d'un nouvel utilisateur (self-registration).")
    public ResponseEntity<UtilisateurDto.Profil> register(@Valid @RequestBody com.educore.modules.auth.dto.InscriptionDto.Requete requete) {
        var profil = authService.inscrire(requete);
        return ResponseEntity.ok(profil);
    }

    @PostMapping("/changer-mot-de-passe")
    @Operation(summary = "Change le mot de passe de l'utilisateur connecte (revoque les autres sessions, emet de nouveaux jetons)")
    public ResponseEntity<ConnexionDto.Reponse> changerMotDePasse(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                   @Valid @RequestBody ChangerMotDePasseDto.Requete requete,
                                                   HttpServletRequest requeteHttp) {
        return ResponseEntity.ok(authService.changerMotDePasse(
                principal.utilisateurId(),
                requete.ancienMotDePasse(),
                requete.nouveauMotDePasse(),
                RequeteUtils.extraireIp(requeteHttp),
                requeteHttp.getHeader("User-Agent")));
    }

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur actuellement connecte")
    public ResponseEntity<UtilisateurDto.Profil> moi(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(authService.obtenirProfil(principal.utilisateurId()));
    }

    @PostMapping("/2fa/initier")
    @Operation(summary = "Demarre l'ACTIVATION du 2FA sur le compte : retourne le secret + l'URI a encoder en QR code (distinct de /2fa/verifier, utilise a la connexion)")
    public ResponseEntity<DeuxFaDto.Provisionnement> initierDeuxFa(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(authService.initierActivationDeuxFa(principal.utilisateurId()));
    }

    @PostMapping("/2fa/confirmer")
    @Operation(summary = "Confirme l'ACTIVATION du 2FA avec un premier code TOTP valide")
    public ResponseEntity<Void> confirmerDeuxFa(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                 @Valid @RequestBody DeuxFaDto.ConfirmerActivation requete) {
        authService.confirmerActivationDeuxFa(principal.utilisateurId(), requete.code());
        return ResponseEntity.noContent().build();
    }
}
