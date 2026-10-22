package com.educore.notifications.security;

/** Identique en esprit au TenantContext du backend principal (Phase 0). */
public final class TenantContext {
    private static final ThreadLocal<Long> ETABLISSEMENT_COURANT = new ThreadLocal<>();
    private TenantContext() {}
    public static void definir(Long etablissementId) { ETABLISSEMENT_COURANT.set(etablissementId); }
    public static Long courant() { return ETABLISSEMENT_COURANT.get(); }
    public static void effacer() { ETABLISSEMENT_COURANT.remove(); }
}
