package com.educore.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception metier de base. Chaque module (auth, eleves, paiements, ...) definit
 * ses propres exceptions specifiques en heritant de celle-ci, avec un code
 * stable (ex: "ELEVE_DEJA_INSCRIT") consomme tel quel par le frontend/mobile
 * pour l'internationalisation (FR/EN) des messages.
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus statut;

    public BusinessException(String code, String message, HttpStatus statut) {
        super(message);
        this.code = code;
        this.statut = statut;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}
