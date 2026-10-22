package com.educore.common.util;

import jakarta.servlet.http.HttpServletRequest;

/** Utilitaires partages pour l'extraction d'informations de la requete HTTP (IP reelle derriere un proxy/ingress). */
public final class RequeteUtils {

    private RequeteUtils() {}

    public static String extraireIp(HttpServletRequest requete) {
        String enTete = requete.getHeader("X-Forwarded-For");
        return (enTete != null && !enTete.isBlank()) ? enTete.split(",")[0].trim() : requete.getRemoteAddr();
    }
}
