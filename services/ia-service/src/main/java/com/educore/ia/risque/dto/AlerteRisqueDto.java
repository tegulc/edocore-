package com.educore.ia.risque.dto;

import java.math.BigDecimal;

public class AlerteRisqueDto {
    public record Profil(Long eleveId, BigDecimal scoreRisque, String niveauRisque, String facteursIdentifies) {}
}
