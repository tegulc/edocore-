package com.educore.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RafraichirDto {
    /** Nom de champ aligne sur ConnexionDto.Reponse.jetonRafraichissement (snake_case sur le fil : jeton_rafraichissement). */
    public record Requete(@NotBlank String jetonRafraichissement) {}
}
