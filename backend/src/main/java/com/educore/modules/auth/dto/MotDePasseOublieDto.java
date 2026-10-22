package com.educore.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MotDePasseOublieDto {
    public record Requete(@NotBlank @Email String email) {}
}
