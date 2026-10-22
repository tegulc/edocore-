package com.educore.notifications.config;

import com.educore.notifications.security.JwtValidationService;
import com.educore.notifications.security.TenantContext;
import com.educore.notifications.security.UtilisateurPrincipal;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Canal temps reel du chat (Module 22). Le handshake HTTP initial est public
 * (voir SecurityConfig), l'authentification se fait via le JWT transmis en
 * parametre de requete (?token=...) au moment de la connexion WebSocket -
 * pattern standard, un navigateur ne pouvant pas placer d'en-tete Authorization
 * sur une connexion WebSocket native.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtValidationService jwtValidationService;

    public WebSocketConfig(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                                    org.springframework.http.server.ServerHttpResponse response,
                                                    org.springframework.web.socket.WebSocketHandler wsHandler,
                                                    Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        String token = extraireParametre(query, "token");
                        if (token == null || !jwtValidationService.estValide(token)) {
                            return false; // rejette la connexion : jeton absent ou invalide
                        }
                        UtilisateurPrincipal principal = jwtValidationService.extraire(token);
                        attributes.put("principal", principal);

                        var authorities = principal.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                        TenantContext.definir(principal.etablissementId());
                        return true;
                    }

                    @Override
                    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                                org.springframework.http.server.ServerHttpResponse response,
                                                org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
                        // rien a faire
                    }

                    private String extraireParametre(String query, String nom) {
                        if (query == null) return null;
                        for (String paire : query.split("&")) {
                            String[] parties = paire.split("=", 2);
                            if (parties.length == 2 && parties[0].equals(nom)) return parties[1];
                        }
                        return null;
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // diffusion aux abonnes (ex: /topic/conversations/{id})
        registry.setApplicationDestinationPrefixes("/app"); // messages entrants des clients
    }
}
