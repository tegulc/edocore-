package com.educore.modules.auth.service;

import com.educore.common.audit.JournalAuditService;
import com.educore.common.exception.BusinessException;
import com.educore.modules.auth.dto.ConnexionDto;
import com.educore.modules.auth.entity.HistoriqueConnexion;
import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.HistoriqueConnexionRepository;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.repository.UtilisateurSessionRepository;
import com.educore.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires des regles metier de connexion (voir Regles 01-08 documentees
 * dans AuthService). Isoles de toute base de donnees reelle (Mockito).
 *
 * Flux 2FA conforme au Volume 6 (Documentation API) : /login ne renvoie jamais
 * d'erreur pour le 2FA, seulement deuxFaRequis=true + un jeton transitoire ;
 * la validation du code se fait via verifierDeuxFaEtConnecter (POST /2fa/verifier).
 */
class AuthServiceTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private UtilisateurRoleService utilisateurRoleService;
    @Mock private UtilisateurSessionRepository sessionRepository;
    @Mock private HistoriqueConnexionRepository historiqueRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private TotpService totpService;
    @Mock private JournalAuditService journalAuditService;

    private AuthService authService;

    @BeforeEach
    void initialiser() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(utilisateurRepository, utilisateurRoleService, sessionRepository,
                historiqueRepository, passwordEncoder, jwtService, totpService, journalAuditService);
        ReflectionTestUtils.setField(authService, "dureeValiditeMotDePasseJours", 90);
        ReflectionTestUtils.setField(authService, "refreshExpirationSecondes", 604800L);
    }

    private Utilisateur utilisateurValide() {
        Utilisateur u = new Utilisateur();
        u.setId(1L);
        u.setEtablissementId(10L);
        u.setEmail("directeur@lycee.cm");
        u.setMotDePasseHash("hash-simule");
        u.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        return u;
    }

    @Test
    void refuseLaConnexionSiEmailInconnu() {
        when(utilisateurRepository.findByEmail("inconnu@lycee.cm")).thenReturn(Optional.empty());

        var requete = new ConnexionDto.Requete("inconnu@lycee.cm", "motdepasse");

        assertThatThrownBy(() -> authService.connecter(requete, "127.0.0.1", "test-agent"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refuseLaConnexionSiMotDePasseIncorrect() {
        Utilisateur u = utilisateurValide();
        when(utilisateurRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mauvais", u.getMotDePasseHash())).thenReturn(false);

        var requete = new ConnexionDto.Requete(u.getEmail(), "mauvais");

        assertThatThrownBy(() -> authService.connecter(requete, "127.0.0.1", "test-agent"))
                .isInstanceOf(BadCredentialsException.class);
        // Regle 03 : l'echec doit tout de meme etre journalise
        org.mockito.Mockito.verify(historiqueRepository).save(any(HistoriqueConnexion.class));
    }

    @Test
    void refuseLaConnexionSiCompteSuspendu() {
        Utilisateur u = utilisateurValide();
        u.setStatut(Utilisateur.StatutUtilisateur.SUSPENDU);
        when(utilisateurRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bonmdp", u.getMotDePasseHash())).thenReturn(true);

        var requete = new ConnexionDto.Requete(u.getEmail(), "bonmdp");

        assertThatThrownBy(() -> authService.connecter(requete, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "COMPTE_INDISPONIBLE");
    }

    @Test
    void retourneUnJetonTransitoireSansExposerLesJetonsDefinitifsSiDeuxFaActive() {
        Utilisateur u = utilisateurValide();
        u.setDeuxFaActive(true);
        u.setDeuxFaSecret("SECRETBASE32");
        when(utilisateurRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bonmdp", u.getMotDePasseHash())).thenReturn(true);
        when(jwtService.genererJetonDeuxFaPending(1L)).thenReturn("jeton-2fa-pending-simule");

        var requete = new ConnexionDto.Requete(u.getEmail(), "bonmdp");
        var reponse = authService.connecter(requete, "127.0.0.1", "test-agent");

        assertThat(reponse.deuxFaRequis()).isTrue();
        assertThat(reponse.jetonDeuxFa()).isEqualTo("jeton-2fa-pending-simule");
        assertThat(reponse.jeton()).isNull();
        assertThat(reponse.jetonRafraichissement()).isNull();
        assertThat(reponse.utilisateur()).isNull();
    }

    @Test
    void verifieDeuxFaEtAcheveLaConnexion() {
        Utilisateur u = utilisateurValide();
        u.setDeuxFaActive(true);
        u.setDeuxFaSecret("SECRETBASE32");

        Role role = new Role();
        role.setId(2L);
        role.setCode("DIRECTEUR");

        when(jwtService.estValide("jeton-2fa-pending-simule")).thenReturn(true);
        when(jwtService.extraireType("jeton-2fa-pending-simule")).thenReturn("deux_fa_pending");
        when(jwtService.extraireUtilisateurId("jeton-2fa-pending-simule")).thenReturn(1L);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));
        when(totpService.verifier("SECRETBASE32", "123456")).thenReturn(true);
        when(utilisateurRoleService.obtenirRolePrincipal(1L)).thenReturn(role);
        when(utilisateurRoleService.listerCodesRoles(1L)).thenReturn(Set.of("DIRECTEUR"));
        when(jwtService.genererAccessToken(eq(1L), eq(10L), eq("DIRECTEUR"), anySet())).thenReturn("access-token-simule");
        when(jwtService.genererRefreshToken(1L)).thenReturn("refresh-token-simule");
        when(jwtService.accessExpirationSecondes()).thenReturn(900L);

        var reponse = authService.verifierDeuxFaEtConnecter("jeton-2fa-pending-simule", "123456", "127.0.0.1", "test-agent");

        assertThat(reponse.deuxFaRequis()).isFalse();
        assertThat(reponse.jeton()).isEqualTo("access-token-simule");
        assertThat(reponse.utilisateur().rolePrincipal()).isEqualTo("DIRECTEUR");
    }

    @Test
    void refuseLaVerificationDeuxFaAvecUnCodeIncorrect() {
        Utilisateur u = utilisateurValide();
        u.setDeuxFaActive(true);
        u.setDeuxFaSecret("SECRETBASE32");

        when(jwtService.estValide("jeton-2fa-pending-simule")).thenReturn(true);
        when(jwtService.extraireType("jeton-2fa-pending-simule")).thenReturn("deux_fa_pending");
        when(jwtService.extraireUtilisateurId("jeton-2fa-pending-simule")).thenReturn(1L);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));
        when(totpService.verifier("SECRETBASE32", "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifierDeuxFaEtConnecter("jeton-2fa-pending-simule", "000000", "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "DEUX_FA_INVALIDE");
    }

    @Test
    void connecteAvecSuccesSansDeuxFa() {
        Utilisateur u = utilisateurValide();

        Role role = new Role();
        role.setId(2L);
        role.setCode("DIRECTEUR");

        when(utilisateurRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bonmdp", u.getMotDePasseHash())).thenReturn(true);
        when(utilisateurRoleService.obtenirRolePrincipal(1L)).thenReturn(role);
        when(utilisateurRoleService.listerCodesRoles(1L)).thenReturn(Set.of("DIRECTEUR"));
        when(jwtService.genererAccessToken(eq(1L), eq(10L), eq("DIRECTEUR"), anySet())).thenReturn("access-token-simule");
        when(jwtService.genererRefreshToken(1L)).thenReturn("refresh-token-simule");
        when(jwtService.accessExpirationSecondes()).thenReturn(900L);

        var requete = new ConnexionDto.Requete(u.getEmail(), "bonmdp");
        var reponse = authService.connecter(requete, "127.0.0.1", "test-agent");

        assertThat(reponse.deuxFaRequis()).isFalse();
        assertThat(reponse.jeton()).isEqualTo("access-token-simule");
        assertThat(reponse.utilisateur().rolePrincipal()).isEqualTo("DIRECTEUR");
        assertThat(reponse.utilisateur().roles()).containsExactly("DIRECTEUR");
    }

    @Test
    void connecteAvecPlusieursRolesCumuleLeursAutorisations() {
        Utilisateur u = utilisateurValide();

        Role rolePrincipal = new Role();
        rolePrincipal.setId(2L);
        rolePrincipal.setCode("PROFESSEUR");

        when(utilisateurRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bonmdp", u.getMotDePasseHash())).thenReturn(true);
        when(utilisateurRoleService.obtenirRolePrincipal(1L)).thenReturn(rolePrincipal);
        when(utilisateurRoleService.listerCodesRoles(1L)).thenReturn(Set.of("PROFESSEUR", "SURVEILLANT_GENERAL"));
        when(jwtService.genererAccessToken(eq(1L), eq(10L), eq("PROFESSEUR"), eq(Set.of("PROFESSEUR", "SURVEILLANT_GENERAL"))))
                .thenReturn("access-token-multi-role");
        when(jwtService.genererRefreshToken(1L)).thenReturn("refresh-token-simule");
        when(jwtService.accessExpirationSecondes()).thenReturn(900L);

        var requete = new ConnexionDto.Requete(u.getEmail(), "bonmdp");
        var reponse = authService.connecter(requete, "127.0.0.1", "test-agent");

        assertThat(reponse.utilisateur().roles()).containsExactlyInAnyOrder("PROFESSEUR", "SURVEILLANT_GENERAL");
    }
}
