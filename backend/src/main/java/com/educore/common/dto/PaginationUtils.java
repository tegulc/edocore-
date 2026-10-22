package com.educore.common.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Construit un Pageable Spring Data a partir des parametres de requete
 * "page" (base 1, defaut 1) et "taille" (defaut 20, plafonne a 100),
 * conformement au Volume 6 (Documentation API), Partie II.2. Volontairement
 * independant de la resolution automatique de Pageable par Spring (qui
 * utilise par defaut "page"/"size" en base 0) pour ne jamais divergre du
 * contrat documente.
 */
public final class PaginationUtils {

    private static final int TAILLE_MAXIMALE = 100;

    private PaginationUtils() {}

    public static Pageable construire(Integer page, Integer taille) {
        int pageEffective = (page == null || page < 1) ? 1 : page;
        int tailleEffective = (taille == null || taille < 1) ? 20 : Math.min(taille, TAILLE_MAXIMALE);
        return PageRequest.of(pageEffective - 1, tailleEffective);
    }

    public static Pageable construire(Integer page, Integer taille, Sort tri) {
        int pageEffective = (page == null || page < 1) ? 1 : page;
        int tailleEffective = (taille == null || taille < 1) ? 20 : Math.min(taille, TAILLE_MAXIMALE);
        return PageRequest.of(pageEffective - 1, tailleEffective, tri);
    }
}
