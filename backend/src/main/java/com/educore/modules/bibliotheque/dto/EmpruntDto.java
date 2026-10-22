package com.educore.modules.bibliotheque.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EmpruntDto {
    public record Creation(@NotNull Long livreId, @NotNull Long utilisateurId, @NotNull LocalDate dateRetourPrevue) {}
    public record Profil(Long id, Long livreId, String livreTitre, Long utilisateurId, LocalDate dateEmprunt,
                          LocalDate dateRetourPrevue, LocalDate dateRetourEffective, String statut) {}
}
