package com.educore.modules.rapport.dto;

import java.math.BigDecimal;

public final class RapportDto {

    private RapportDto() {}

    public record EffectifClasse(Long classeId, String libelle, Long effectif, Integer capacite) {}

    public record SoldeEleve(Long eleveId, String nom, String prenom, BigDecimal totalDu, BigDecimal totalPaye, BigDecimal soldeRestant) {}

    public record TauxPresenceEleve(Long eleveId, String nom, String prenom, Long joursPresents, Long joursAbsents, Double tauxPresence) {}

    public record MoyenneClasse(Long eleveId, String nom, String prenom, BigDecimal moyenne, Integer rang) {}

    public record Synthese(
            long elevesActifs,
            double tauxPresenceMoyen,
            BigDecimal recettesMois,
            long incidentsDisciplinaires,
            long congesEnAttente
    ) {}
}
