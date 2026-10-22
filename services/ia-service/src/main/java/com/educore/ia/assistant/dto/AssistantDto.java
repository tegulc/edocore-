package com.educore.ia.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class AssistantDto {
    public record Question(@NotBlank String question) {}
    public record Echange(Long id, String question, String reponse, String horodatage) {}
}
