package com.educore.modules.eleve.dto;

import jakarta.validation.constraints.NotNull;

public class InscriptionDto {
    public record Creation(@NotNull Long classeId, String typeInscription) {}

    public record Profil(Long id, Long eleveId, Long anneeScolaireId, Long classeId, String classeLibelle,
                          String typeInscription, String statut) {}
}
