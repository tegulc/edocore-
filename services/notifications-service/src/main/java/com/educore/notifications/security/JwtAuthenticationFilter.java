package com.educore.notifications.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Identique en esprit au filtre du backend principal (Phase 0/1) : une GrantedAuthority par role attribue. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidationService jwtValidationService;

    public JwtAuthenticationFilter(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String enTete = request.getHeader("Authorization");
        if (enTete != null && enTete.startsWith("Bearer ")) {
            String token = enTete.substring(7);
            if (jwtValidationService.estValide(token)) {
                UtilisateurPrincipal principal = jwtValidationService.extraire(token);
                var authorities = principal.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                TenantContext.definir(principal.etablissementId());
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.effacer();
        }
    }
}
