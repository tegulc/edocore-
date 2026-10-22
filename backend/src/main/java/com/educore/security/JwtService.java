package com.educore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Emission et validation des jetons JWT (access, refresh, et jeton transitoire de
 * verification 2FA). Durees exprimees en SECONDES, conformement a la variable
 * d'environnement JWT_EXPIRATION_SECONDS documentee au Volume 7 (Guide
 * d'installation), Partie III.1.
 *
 * Chaque jeton porte un claim "type" (access | refresh | deux_fa_pending) :
 * JwtAuthenticationFilter refuse tout jeton dont le type n'est pas "access"
 * pour authentifier une requete API (voir Regle de securite dans ce filtre).
 */
@Service
public class JwtService {

    @Value("${educore.security.jwt.secret}")
    private String secret;

    @Value("${educore.security.jwt.access-expiration-seconds}")
    private long accessExpirationSecondes;

    @Value("${educore.security.jwt.refresh-expiration-seconds}")
    private long refreshExpirationSecondes;

    @Value("${educore.security.jwt.deux-fa-pending-expiration-seconds}")
    private long deuxFaPendingExpirationSecondes;

    private SecretKey cle() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String genererAccessToken(Long utilisateurId, Long etablissementId, String rolePrincipal, Set<String> roles) {
        return Jwts.builder()
                .subject(String.valueOf(utilisateurId))
                .claims(Map.of(
                        "etablissementId", etablissementId,
                        "role", rolePrincipal,
                        "roles", List.copyOf(roles),
                        "type", "access"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationSecondes * 1000))
                .signWith(cle())
                .compact();
    }

    public String genererRefreshToken(Long utilisateurId) {
        return Jwts.builder()
                .subject(String.valueOf(utilisateurId))
                .claims(Map.of("type", "refresh"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationSecondes * 1000))
                .signWith(cle())
                .compact();
    }

    /**
     * Jeton transitoire tres court (2 min par defaut), emis apres verification
     * du mot de passe quand le 2FA est actif. Ne donne acces a rien d'autre
     * qu'a POST /auth/2fa/verifier (voir claim "type" = deux_fa_pending).
     */
    public String genererJetonDeuxFaPending(Long utilisateurId) {
        return Jwts.builder()
                .subject(String.valueOf(utilisateurId))
                .claims(Map.of("type", "deux_fa_pending"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + deuxFaPendingExpirationSecondes * 1000))
                .signWith(cle())
                .compact();
    }

    /**
     * Jeton pour les integrations partenaires (API publique, Phase 9 - Volume 6
     * §OAuth2). Claim "type" = api_client, distinct de "access" : ce jeton
     * n'authentifie JAMAIS un utilisateur (voir JwtAuthenticationFilter), il
     * porte uniquement le contexte etablissement + une identite client, pour
     * un acces volontairement plus restreint (endpoints publics dedies).
     */
    public String genererJetonApiClient(Long etablissementId, String clientId) {
        return Jwts.builder()
                .subject(clientId)
                .claims(Map.of("etablissementId", etablissementId, "type", "api_client"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1h, standard OAuth2 client_credentials
                .signWith(cle())
                .compact();
    }

    public long accessExpirationSecondes() {
        return accessExpirationSecondes;
    }

    public Long extraireUtilisateurId(String token) {
        return Long.valueOf(extraireClaim(token, Claims::getSubject));
    }

    public String extraireType(String token) {
        return extraireClaim(token, claims -> String.valueOf(claims.get("type")));
    }

    public Claims extraireClaims(String token) {
        return Jwts.parser().verifyWith(cle()).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean estValide(String token) {
        try {
            return extraireExpiration(token).after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Date extraireExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(cle()).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }
}
