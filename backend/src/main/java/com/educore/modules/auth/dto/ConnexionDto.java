package com.educore.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrat conforme au Volume 6 (Documentation API), Partie IV.1 : les noms
 * de champs (jeton, jeton_rafraichissement, expire_dans, deux_fa_requis)
 * correspondent exactement a l'exemple documente. La conversion camelCase
 * (cote Java) <-> snake_case (sur le fil) est geree globalement par Jackson
 * (voir application.yml, spring.jackson.property-naming-strategy).
 */
public class ConnexionDto {

    /** Etape 1 : email + mot de passe uniquement (le code 2FA, s'il est requis, est verifie a l'etape 2, voir DeuxFaDto). */
    public record Requete(@NotBlank @Email String email, @NotBlank String motDePasse) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Reponse(
            String jeton,
            String jetonRafraichissement,
            Long expireDans,
            boolean deuxFaRequis,
            /** Present uniquement quand deuxFaRequis = true : a representer tel quel a POST /auth/2fa/verifier. */
            String jetonDeuxFa,
            /** Absent quand deuxFaRequis = true (le profil n'est retourne qu'une fois l'authentification vraiment terminee). */
            UtilisateurDto.Profil utilisateur
    ) {
        public static Reponse deuxFaRequise(String jetonDeuxFa) {
            return new Reponse(null, null, null, true, jetonDeuxFa, null);
        }

        public static Reponse succes(String jeton, String jetonRafraichissement, long expireDans, UtilisateurDto.Profil profil) {
            return new Reponse(jeton, jetonRafraichissement, expireDans, false, null, profil);
        }
    }
}
