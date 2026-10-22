package com.educore.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Format standard de reponse d'erreur pour toute l'API EDUCORE, conforme au
 * Volume 6 (Documentation API), Partie II.3 : toute erreur est enveloppee
 * dans un objet racine "erreur" avec code/message/champ.
 *
 * Extension additive (non documentee explicitement, mais le Volume 6 precise
 * que ses exemples "ne pretendent pas a l'exhaustivite") : "champs" (pluriel)
 * porte la liste complete des erreurs de validation quand plusieurs champs
 * sont en cause simultanement, sans quoi une erreur de formulaire multi-champs
 * perdrait de l'information. "champ" (singulier, documente) reste rempli
 * quand une seule erreur de champ est concernee, pour rester compatible
 * avec l'exemple exact du Volume 6 (§4.4).
 */
public record ApiError(Contenu erreur) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Contenu(String code, String message, String champ, List<ChampErreur> champs) {}

    public record ChampErreur(String champ, String message) {}

    public static ApiError de(String code, String message) {
        return new ApiError(new Contenu(code, message, null, null));
    }

    public static ApiError deChamp(String code, String message, String champ) {
        return new ApiError(new Contenu(code, message, champ, null));
    }

    public static ApiError deValidation(String code, String message, List<ChampErreur> champs) {
        String champUnique = champs.size() == 1 ? champs.get(0).champ() : null;
        return new ApiError(new Contenu(code, message, champUnique, champs));
    }
}
