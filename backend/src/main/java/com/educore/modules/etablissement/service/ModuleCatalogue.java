package com.educore.modules.etablissement.service;

import java.util.Set;

/**
 * Catalogue des modules activables/desactivables par etablissement (cahier
 * des charges, 3.1 Administrateur). AUTH et ETABLISSEMENT ne figurent pas
 * ici : ce sont des modules socle, toujours actifs, non desactivables.
 *
 * Seuls les modules deja livres sont surs d'etre fonctionnels si actives ;
 * les autres sont references des maintenant pour que l'ecran d'administration
 * des modules (Phase 2) n'ait pas besoin d'etre retouche a chaque nouvelle
 * livraison - activer un module non encore construit n'a simplement aucun effet.
 */
public final class ModuleCatalogue {

    public static final Set<String> MODULES_DISPONIBLES = Set.of(
            "ELEVES", "PARENTS", "ENSEIGNANTS", "CLASSES", "EMPLOI_DU_TEMPS",
            "PRESENCE", "NOTES", "BULLETINS", "CAHIER_TEXTE", "DEVOIRS", "DISCIPLINE",
            "PAIEMENTS", "COMPTABILITE",
            "BIBLIOTHEQUE", "INFIRMERIE", "TRANSPORT", "CANTINE",
            "PERSONNEL", "DOCUMENTS",
            "CHAT", "REUNIONS", "NOTIFICATIONS",
            "IA", "DASHBOARD", "RAPPORTS"
    );

    /** Modules actives par defaut a la creation d'un etablissement (coeur pedagogique minimal). */
    public static final Set<String> MODULES_ACTIFS_PAR_DEFAUT = Set.of(
            "ELEVES", "PARENTS", "ENSEIGNANTS", "CLASSES", "NOTES", "BULLETINS", "PRESENCE"
    );

    private ModuleCatalogue() {}
}
