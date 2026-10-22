package com.educore.modules.auth;

import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.entity.UtilisateurRole;
import com.educore.modules.auth.repository.RoleRepository;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.repository.UtilisateurRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'integration bout-en-bout : requete HTTP reelle -> filtre securite ->
 * controller -> service -> base H2 en memoire. Les corps de requete/reponse
 * JSON utilisent le contrat en snake_case du Volume 6 (Documentation API) ;
 * la table Map.of(...) ci-dessous represente le JSON brut, pas des noms de
 * champ Java (la naming strategy Jackson ne s'applique qu'aux POJO, pas aux Map).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UtilisateurRoleRepository utilisateurRoleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String MOT_DE_PASSE_CLAIR = "MotDePasseSolide123";

    @BeforeEach
    void creerUtilisateurDeTest() {
        Role role = new Role();
        role.setCode("DIRECTEUR");
        role.setLibelle("Directeur");
        roleRepository.save(role);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEtablissementId(1L);
        utilisateur.setNom("Mballa");
        utilisateur.setPrenom("Jean");
        utilisateur.setEmail("directeur@lycee-test.cm");
        utilisateur.setMotDePasseHash(passwordEncoder.encode(MOT_DE_PASSE_CLAIR));
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        UtilisateurRole attribution = new UtilisateurRole();
        attribution.setUtilisateurId(utilisateur.getId());
        attribution.setRoleId(role.getId());
        attribution.setEstPrincipal(true);
        utilisateurRoleRepository.save(attribution);
    }

    @Test
    void connexionReussieRetourneUnJetonEtLeProfil() throws Exception {
        var corps = Map.of("email", "directeur@lycee-test.cm", "mot_de_passe", MOT_DE_PASSE_CLAIR);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corps)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jeton").isNotEmpty())
                .andExpect(jsonPath("$.jeton_rafraichissement").isNotEmpty())
                .andExpect(jsonPath("$.deux_fa_requis").value(false))
                .andExpect(jsonPath("$.utilisateur.email").value("directeur@lycee-test.cm"))
                .andExpect(jsonPath("$.utilisateur.role_principal").value("DIRECTEUR"));
    }

    @Test
    void connexionEchoueeAvecMauvaisMotDePasseRetourne401() throws Exception {
        var corps = Map.of("email", "directeur@lycee-test.cm", "mot_de_passe", "mauvais-mot-de-passe");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corps)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erreur.code").value("IDENTIFIANTS_INVALIDES"));
    }

    @Test
    void accesRefuseAEndpointProtegeSansJeton() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRetourneLeProfilQuandLeJetonEstValide() throws Exception {
        var corpsConnexion = Map.of("email", "directeur@lycee-test.cm", "mot_de_passe", MOT_DE_PASSE_CLAIR);

        String reponseJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpsConnexion)))
                .andReturn().getResponse().getContentAsString();

        String jeton = objectMapper.readTree(reponseJson).get("jeton").asText();

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("directeur@lycee-test.cm"));
    }

    @Test
    void unRefreshTokenNePeutPasServirDAccessTokenSurUnEndpointProtege() throws Exception {
        var corpsConnexion = Map.of("email", "directeur@lycee-test.cm", "mot_de_passe", MOT_DE_PASSE_CLAIR);

        String reponseJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpsConnexion)))
                .andReturn().getResponse().getContentAsString();

        String jetonRafraichissement = objectMapper.readTree(reponseJson).get("jeton_rafraichissement").asText();

        // Regression test de la faille corrigee : un refresh token presente comme Bearer
        // ne doit authentifier personne (claim "type" != "access").
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jetonRafraichissement))
                .andExpect(status().isUnauthorized());
    }
}
