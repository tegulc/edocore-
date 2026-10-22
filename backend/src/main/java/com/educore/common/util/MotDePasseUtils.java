package com.educore.common.util;

import java.security.SecureRandom;

/**
 * Generation de mots de passe temporaires (creation de compte, bootstrap
 * d'etablissement...). Alphabet volontairement prive des caracteres ambigus
 * (0/O, 1/l/I) pour rester lisible quand communique oralement ou par SMS.
 */
public final class MotDePasseUtils {

    private static final String MAJUSCULES = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijkmnopqrstuvwxyz";
    private static final String CHIFFRES = "23456789";
    private static final String TOUT = MAJUSCULES + MINUSCULES + CHIFFRES;
    private static final SecureRandom RANDOM = new SecureRandom();

    private MotDePasseUtils() {}

    /** Garantit la conformite a la politique de complexite (au moins 1 majuscule, 1 minuscule, 1 chiffre). */
    public static String genererMotDePasseTemporaire() {
        StringBuilder motDePasse = new StringBuilder();
        motDePasse.append(MAJUSCULES.charAt(RANDOM.nextInt(MAJUSCULES.length())));
        motDePasse.append(MINUSCULES.charAt(RANDOM.nextInt(MINUSCULES.length())));
        motDePasse.append(CHIFFRES.charAt(RANDOM.nextInt(CHIFFRES.length())));
        for (int i = 0; i < 9; i++) {
            motDePasse.append(TOUT.charAt(RANDOM.nextInt(TOUT.length())));
        }
        // Melange pour ne pas avoir systematiquement Maj+min+chiffre dans le meme ordre en tete.
        char[] caracteres = motDePasse.toString().toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = caracteres[i]; caracteres[i] = caracteres[j]; caracteres[j] = tmp;
        }
        return new String(caracteres);
    }
}
