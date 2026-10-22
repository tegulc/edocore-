package com.educore.notifications.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Meme contrat que educore-backend (Volume 6 §2.3) - conserve la coherence cote clients. */
public record ApiError(Contenu erreur) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Contenu(String code, String message) {}
    public static ApiError de(String code, String message) { return new ApiError(new Contenu(code, message)); }
}
