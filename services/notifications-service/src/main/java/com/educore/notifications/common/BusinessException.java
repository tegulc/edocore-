package com.educore.notifications.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus statut;
    public BusinessException(String code, String message, HttpStatus statut) {
        super(message);
        this.code = code;
        this.statut = statut;
    }
    public String getCode() { return code; }
    public HttpStatus getStatut() { return statut; }
}
