package com.educore.security;

import java.util.Set;

/**
 * Contexte de securite courant, extrait du JWT a chaque requete.
 * rolePrincipal : role par defaut pour le contexte UI/JWT.
 * roles : ensemble complet des roles attribues (cahier des charges Module 1 -
 *         support multi-role), utilise pour le RBAC (@PreAuthorize, PermissionService).
 */
public record UtilisateurPrincipal(
        Long utilisateurId,
        Long etablissementId,
        String rolePrincipal,
        Set<String> roles
) {}
