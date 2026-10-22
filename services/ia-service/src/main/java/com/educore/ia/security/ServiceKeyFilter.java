package com.educore.ia.security;

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

/** Identique en principe a notifications-service : appels systeme educore-backend -> ia-service (ex: "recalculer les alertes"). */
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
