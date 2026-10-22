package com.educore.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extrait le JWT de l'en-tete Authorization, le valide, puis peuple le
 * SecurityContext avec un {@link UtilisateurPrincipal} (utilisateur + tenant + roles).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String enTete = request.getHeader("Authorization");
        if (enTete != null && enTete.startsWith("Bearer ")) {
            try {
                String token = enTete.substring(7);
                if (jwtService.estValide(token)) {
                    String type = jwtService.extraireType(token);
                    Claims claims = jwtService.extraireClaims(token);

                    if ("access".equals(type)) {
                        Long utilisateurId = Long.valueOf(claims.getSubject());
                        Long etablissementId = Long.valueOf(String.valueOf(claims.get("etablissementId")));
                        String rolePrincipal = String.valueOf(claims.get("role"));
                        List<String> rolesBrut = claims.get("roles", List.class);
                        Set<String> roles = rolesBrut != null ? Set.copyOf(rolesBrut) : Set.of(rolePrincipal);

                        UtilisateurPrincipal principal = new UtilisateurPrincipal(utilisateurId, etablissementId, rolePrincipal, roles);

                        List<GrantedAuthority> autorites = roles.stream()
                                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                                .collect(Collectors.toList());

                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, autorites));
                        TenantContext.definir(etablissementId);

                    } else if ("api_client".equals(type)) {
                        String clientId = claims.getSubject();
                        Long etablissementId = Long.valueOf(String.valueOf(claims.get("etablissementId")));

                        ApiClientPrincipal principal = new ApiClientPrincipal(clientId, etablissementId);
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))));
                        TenantContext.definir(etablissementId);
                    }
                }
            } catch (Exception ignored) {
                // Jeton absent, expire ou invalide : la requete continue en anonyme
                // (ex. POST /auth/login ne doit jamais echouer a cause d'un vieux Bearer).
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.effacer();
        }
    }
}
