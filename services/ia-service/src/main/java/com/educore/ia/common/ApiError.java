package com.educore.ia.common;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiError(Contenu erreur) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Contenu(String code, String message) {}
    public static ApiError de(String code, String message) { return new ApiError(new Contenu(code, message)); }
}
