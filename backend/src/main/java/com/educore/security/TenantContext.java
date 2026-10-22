package com.educore.security;

/**
 * Porte l'identifiant de l'etablissement courant pendant la duree d'une requete.
 * Consomme par un filtre Hibernate global pour garantir qu'aucune requete ne
 * peut, meme par erreur de developpement, lire ou ecrire les donnees d'un
 * autre etablissement que celui de l'utilisateur connecte.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> ETABLISSEMENT_COURANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void definir(Long etablissementId) {
        ETABLISSEMENT_COURANT.set(etablissementId);
    }

    public static Long courant() {
        return ETABLISSEMENT_COURANT.get();
    }

    public static void effacer() {
        ETABLISSEMENT_COURANT.remove();
    }
}
