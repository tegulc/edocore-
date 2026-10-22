package com.educore.modules.bibliotheque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LivreDto {
    public record Creation(@NotBlank String titre, String auteur, String editeur, String categorie, String isbn, @NotNull @Positive Integer nombreExemplaires) {}
    public record Profil(Long id, String isbn, String titre, String auteur, String editeur, String categorie,
                          Integer nombreExemplaires, Integer exemplairesDisponibles, String qrCode) {}
}
