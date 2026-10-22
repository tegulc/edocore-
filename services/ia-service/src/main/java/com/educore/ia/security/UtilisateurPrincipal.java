package com.educore.ia.security;

import java.util.Set;

public record UtilisateurPrincipal(Long utilisateurId, Long etablissementId, String rolePrincipal, Set<String> roles) {}
