package com.educore.notifications.security;

import java.util.Set;

/** Identique en forme au principal du backend principal - reconstruit localement a partir du meme JWT. */
public record UtilisateurPrincipal(Long utilisateurId, Long etablissementId, String rolePrincipal, Set<String> roles) {}
