package com.educore.notifications.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Valide localement les JWT emis par educore-backend (POST /auth/login), sans
 * jamais rappeler ce service pour verifier un jeton : la verification de
 * signature HMAC ne necessite que le secret partage (JWT_SECRET, meme valeur
 * des deux cotes), pas d'appel reseau. C'est precisement l'avantage du JWT
 * stateless deja pose en Phase 1 - il rend cette separation de service peu
 * couteuse plutot que de necessiter une nouvelle solution d'authentification.
 */
@Service
public class JwtValidationService {

    @Value("${educore.security.jwt.secret}")
    private String secret;

    private SecretKey cle() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean estValide(String token) {
        try {
            Claims claims = extraireClaims(token);
            return claims.getExpiration().after(new Date()) && "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public UtilisateurPrincipal extraire(String token) {
        Claims claims = extraireClaims(token);
        Long utilisateurId = Long.valueOf(claims.getSubject());
        Long etablissementId = Long.valueOf(String.valueOf(claims.get("etablissementId")));
        String role = String.valueOf(claims.get("role"));
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return new UtilisateurPrincipal(utilisateurId, etablissementId, role, Set.copyOf(roles));
    }

    private Claims extraireClaims(String token) {
        return Jwts.parser().verifyWith(cle()).build().parseSignedClaims(token).getPayload();
    }
}
