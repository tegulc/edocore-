package com.educore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Limite le nombre de tentatives de connexion par adresse IP (Phase 9 -
 * securite avancee, mentionnee dans le document d'architecture depuis la
 * Phase 0 mais jamais cablee jusqu'ici). Fenetre glissante simple (compteur
 * Redis avec expiration), suffisante pour freiner un bourrage d'identifiants
 * sans l'over-engineering d'un algorithme de type token bucket distribue.
 *
 * Volontairement limite a /auth/login (la cible la plus sensible d'un
 * bourrage d'identifiants) plutot qu'applique globalement : un rate limiting
 * generique sur toute l'API relevera d'une API Gateway dediee (Kong, voir
 * Volume 2 §2.4) le jour ou la charge le justifiera.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE_TENTATIVES = 10;
    private static final Duration FENETRE = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String cle = "ratelimit:login:" + extraireIp(request);
            Long tentatives = redisTemplate.opsForValue().increment(cle);
            if (tentatives != null && tentatives == 1L) {
                redisTemplate.expire(cle, FENETRE);
            }
            if (tentatives != null && tentatives > LIMITE_TENTATIVES) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"erreur\":{\"code\":\"TROP_DE_TENTATIVES\",\"message\":\"Trop de tentatives de connexion. Reessayez dans une minute.\"}}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String extraireIp(HttpServletRequest request) {
        String enTete = request.getHeader("X-Forwarded-For");
        return (enTete != null && !enTete.isBlank()) ? enTete.split(",")[0].trim() : request.getRemoteAddr();
    }
}
