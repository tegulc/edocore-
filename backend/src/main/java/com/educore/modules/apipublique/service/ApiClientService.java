package com.educore.modules.apipublique.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.apipublique.dto.ApiClientDto;
import com.educore.modules.apipublique.entity.ApiClient;
import com.educore.modules.apipublique.repository.ApiClientRepository;
import com.educore.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * API publique (Module 30, Phase 9) : integrations partenaires (Moodle,
 * Microsoft 365, Google Workspace...) via OAuth2 client_credentials. Un
 * client API n'est jamais un utilisateur - il recoit un jeton distinct
 * (voir JwtService.genererJetonApiClient), scope au strict necessaire par
 * les controllers publics dedies (pas d'acces aux endpoints normaux).
 */
@Service
public class ApiClientService {

    private final ApiClientRepository apiClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ApiClientService(ApiClientRepository apiClientRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.apiClientRepository = apiClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiClientDto.CreationReponse creer(Long etablissementId, ApiClientDto.Creation requete) {
        String clientId = "client_" + genererChaineAleatoire(16);
        String clientSecret = genererChaineAleatoire(40);

        ApiClient client = new ApiClient();
        client.setEtablissementId(etablissementId);
        client.setNom(requete.nom());
        client.setClientId(clientId);
        client.setClientSecretHash(passwordEncoder.encode(clientSecret));
        apiClientRepository.save(client);

        // Le clientSecret n'est jamais stocke en clair ni recuperable ensuite : affiche une seule fois ici.
        return new ApiClientDto.CreationReponse(client.getId(), client.getNom(), clientId, clientSecret);
    }

    public List<ApiClientDto.Profil> lister(Long etablissementId) {
        return apiClientRepository.findByEtablissementId(etablissementId).stream()
                .map(c -> new ApiClientDto.Profil(c.getId(), c.getNom(), c.getClientId(), c.getStatut()))
                .toList();
    }

    public ApiClientDto.ReponseJeton authentifier(ApiClientDto.DemandeJeton requete) {
        if (!"client_credentials".equals(requete.grantType())) {
            throw new BusinessException("GRANT_TYPE_NON_SUPPORTE", "Seul client_credentials est supporte.", HttpStatus.BAD_REQUEST);
        }
        ApiClient client = apiClientRepository.findByClientId(requete.clientId())
                .orElseThrow(() -> new BusinessException("CLIENT_INVALIDE", "Identifiants client invalides.", HttpStatus.UNAUTHORIZED));

        if (!"actif".equals(client.getStatut()) || !passwordEncoder.matches(requete.clientSecret(), client.getClientSecretHash())) {
            throw new BusinessException("CLIENT_INVALIDE", "Identifiants client invalides.", HttpStatus.UNAUTHORIZED);
        }

        String jeton = jwtService.genererJetonApiClient(client.getEtablissementId(), client.getClientId());
        return new ApiClientDto.ReponseJeton(jeton, "Bearer", 3600);
    }

    private String genererChaineAleatoire(int longueurOctets) {
        byte[] octets = new byte[longueurOctets];
        new SecureRandom().nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }
}
