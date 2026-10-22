package com.educore.notifications.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authentifie les appels SERVICE-A-SERVICE depuis educore-backend (ex: "bulletin
 * publie -> creer une notification"), qui n'ont pas de contexte utilisateur JWT.
 * En-tete X-Service-Key compare a educore.security.service-key (meme valeur
 * des deux cotes). Accorde le role interne ROLE_SERVICE_INTERNE, jamais un
 * role metier - ces appels ne representent aucun utilisateur reel.
 */
@Component
public class ServiceKeyFilter extends OncePerRequestFilter {

    @Value("${educore.security.service-key}")
    private String cleService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cleRecue = request.getHeader("X-Service-Key");
        if (cleRecue != null && cleRecue.equals(cleService) && !cleService.isBlank()) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "service-interne", null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE_INTERNE")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
