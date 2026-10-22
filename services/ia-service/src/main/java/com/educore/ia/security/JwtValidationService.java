package com.educore.ia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;

/** Identique en principe a notifications-service : validation locale via le meme JWT_SECRET, sans appel reseau. */
@Service
public class JwtValidationService {

    @Value("${educore.security.jwt.secret}")
    private String secret;

    private SecretKey cle() { return Keys.hmacShaKeyFor(secret.getBytes()); }

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
