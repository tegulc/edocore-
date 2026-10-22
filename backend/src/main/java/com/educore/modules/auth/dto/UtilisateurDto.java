package com.educore.modules.auth.dto;

import java.util.Set;

public class UtilisateurDto {

    /**
     * Profil public de l'utilisateur, retourne apres connexion et sur /me. Jamais de hash ni de secret 2FA ici.
     * rolePrincipal : role par defaut (contexte JWT/UI). roles : ensemble complet des roles attribues
     * (cahier des charges Module 1 - support multi-role).
     */
    public record Profil(
            Long id,
            Long etablissementId,
            String rolePrincipal,
            Set<String> roles,
            String matricule,
            String nom,
            String prenom,
            String email,
            String telephone,
            String photoUrl,
            String languePreferee,
            boolean deuxFaActive,
            boolean doitChangerMotDePasse
    ) {}
}
