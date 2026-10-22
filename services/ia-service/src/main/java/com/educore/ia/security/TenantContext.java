package com.educore.ia.security;

public final class TenantContext {
    private static final ThreadLocal<Long> ETABLISSEMENT_COURANT = new ThreadLocal<>();
    private TenantContext() {}
    public static void definir(Long etablissementId) { ETABLISSEMENT_COURANT.set(etablissementId); }
    public static Long courant() { return ETABLISSEMENT_COURANT.get(); }
    public static void effacer() { ETABLISSEMENT_COURANT.remove(); }
}
