package com.educore.common.exception;

import org.springframework.http.HttpStatus;

/** Levee lorsqu'une ressource (eleve, classe, facture...) demandee n'existe pas ou n'appartient pas au tenant courant. */
public class RessourceIntrouvableException extends BusinessException {
    public RessourceIntrouvableException(String ressource, Object identifiant) {
        super("RESSOURCE_INTROUVABLE",
                "%s introuvable pour l'identifiant : %s".formatted(ressource, identifiant),
                HttpStatus.NOT_FOUND);
    }
}
