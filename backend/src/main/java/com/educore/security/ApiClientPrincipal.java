package com.educore.security;

/** Contexte d'un client API partenaire authentifie (Phase 9, API publique). Jamais un utilisateur reel. */
public record ApiClientPrincipal(String clientId, Long etablissementId) {}
