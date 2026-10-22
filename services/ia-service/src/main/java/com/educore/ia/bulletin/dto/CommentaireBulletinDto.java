package com.educore.ia.bulletin.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentaireBulletinDto {
    public record Profil(Long id, Long bulletinId, String commentaireGenere, boolean valideParEnseignant) {}
    public record Validation(@NotBlank String commentaireFinal) {}
}
