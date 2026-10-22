package com.educore.ia.common;

import org.springframework.http.HttpStatus;

public class RessourceIntrouvableException extends BusinessException {
    public RessourceIntrouvableException(String ressource, Object id) {
        super("RESSOURCE_INTROUVABLE", "%s introuvable pour l'identifiant : %s".formatted(ressource, id), HttpStatus.NOT_FOUND);
    }
}
